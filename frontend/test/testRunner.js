import assert from "assert";
import { fileURLToPath, pathToFileURL } from "url";
import { dirname, join } from "path";
import { readdirSync } from "fs";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

let testCount = 0;
let passCount = 0;
let failCount = 0;

export function describe(name, fn) {
  console.log(`\n${name}`);
  fn();
}

export function it(name, fn) {
  testCount++;
  try {
    fn();
    passCount++;
    console.log(`  ✓ ${name}`);
  } catch (error) {
    failCount++;
    console.log(`  ✗ ${name}`);
    console.log(`    Error: ${error.message}`);
  }
}

export function expect(value) {
  return {
    toBe: (expected) => assert.strictEqual(value, expected),
    toEqual: (expected) => assert.deepStrictEqual(value, expected),
    toContain: (substring) => assert(value.includes(substring), `Expected "${value}" to contain "${substring}"`),
    toBeNull: () => assert.strictEqual(value, null),
    toBeDefined: () => assert.notStrictEqual(value, undefined),
    toBeTruthy: () => assert.ok(value),
    toBeFalsy: () => assert.ok(!value),
    toThrow: () => {
      try {
        value();
        throw new Error("Expected function to throw");
      } catch (e) {
        if (e.message === "Expected function to throw") throw e;
      }
    }
  };
}

// Load and run all test files
async function runTests() {
  const testDir = join(__dirname);
  const testFiles = readdirSync(testDir).filter(f => f.startsWith("test_") && f.endsWith(".js"));

  for (const file of testFiles) {
    const filePath = join(testDir, file);
    const fileUrl = pathToFileURL(filePath).href;
    await import(fileUrl);
  }

  console.log("\n" + "=".repeat(50));
  console.log(`Tests: ${passCount} passed, ${failCount} failed (${testCount} total)`);
  console.log("=".repeat(50) + "\n");

  process.exit(failCount > 0 ? 1 : 0);
}

runTests().catch(console.error);
