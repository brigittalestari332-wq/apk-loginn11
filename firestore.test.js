const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validProfilePayload(uid, now) {
  return {
    userId: uid,
    fullName: "Nabila Samudra",
    username: "nabila_laut",
    email: "nabila@example.com",
    occupation: "Desainer UI",
    city: "Surabaya",
    loginCount: 1,
    createdAt: now,
    updatedAt: now,
    lastLoginAt: now,
  };
}

test("Unauthenticated user cannot read, list, or write user profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now))
  );
});

test("Authenticated owner can register, read own profile, edit profile, and record login", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created))
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Edit profile action
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      fullName: "Nabila Samudra Putri",
      username: "nabila.samudra",
      occupation: "Product Designer",
      city: "Bandung",
      updatedAt: new Date(Date.now() - 2000),
    })
  );

  // Record login session action
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      loginCount: 2,
      lastLoginAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Authenticated user cannot list all users or access another user's PII profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const now = new Date(Date.now() - 5000);
    await context.firestore().collection("users").doc(BOB_UID).set(validProfilePayload(BOB_UID, now));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      fullName: "Hacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Rules reject shadow fields, identity spoofing, invalid username, and immutable field edits", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  // Ghost field rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...validProfilePayload(ALICE_UID, created),
      isAdmin: true,
    })
  );

  // Spoofed userId rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...validProfilePayload(BOB_UID, created),
    })
  );

  // Invalid username (spaces) rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...validProfilePayload(ALICE_UID, created),
      username: "bad username",
    })
  );

  // Future timestamp rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set(
      validProfilePayload(ALICE_UID, new Date(Date.now() + 60000))
    )
  );

  // Create valid doc then test immutable & invalid update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created))
  );

  // Cannot mutate createdAt
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cannot jump loginCount by +5
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      loginCount: 6,
      lastLoginAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
