package app.gamenative

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gamenative.Crypto
import java.security.SecureRandom
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoTest {

    @Test
    fun encryptDecrypt_withString_returnsOriginalString() {
        val originalString = "Hello, World!"
        val originalBytes = originalString.toByteArray()

        val encryptedBytes = Crypto.encrypt(originalBytes)
        val decryptedBytes = Crypto.decrypt(encryptedBytes)
        val decryptedString = String(decryptedBytes)

        assertNotEquals(
            "Encrypted bytes should be different from original",
            originalBytes.contentToString(),
            encryptedBytes.contentToString(),
        )
        assertEquals("Decrypted string should match original", originalString, decryptedString)
    }

    @Test
    fun encryptDecrypt_withLargeData_succeeds() {
        val random = SecureRandom()
        val largeData = ByteArray(1024 * 1024) // 1MB
        random.nextBytes(largeData)

        val encryptedBytes = Crypto.encrypt(largeData)
        val decryptedBytes = Crypto.decrypt(encryptedBytes)

        assertTrue("Decrypted data should match original", largeData.contentEquals(decryptedBytes))
    }

    @Test
    fun encryptDecrypt_withEmptyData_throwsException() {
        val emptyData = ByteArray(0)

        try {
            Crypto.encrypt(emptyData)
            fail("Should have thrown IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun decrypt_withInvalidData_throwsException() {
        val invalidData = ByteArray(10)

        try {
            Crypto.decrypt(invalidData)
            fail("Should have thrown IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun encrypt_producesRandomOutput() {
        val input = "Test".toByteArray()

        val firstEncryption = Crypto.encrypt(input)
        val secondEncryption = Crypto.encrypt(input)

        assertNotEquals(
            "Multiple encryptions of same data should produce different results",
            firstEncryption.contentToString(),
            secondEncryption.contentToString(),
        )
    }

    @Test
    fun encryptDecrypt_withSpecialCharacters_succeeds() {
        val specialChars = "!@#$%^&*()_+-=[]{}|;:'\",.<>?/~`"
        val originalBytes = specialChars.toByteArray()

        val encryptedBytes = Crypto.encrypt(originalBytes)
        val decryptedBytes = Crypto.decrypt(encryptedBytes)

        assertTrue(
            "Decrypted special characters should match original",
            originalBytes.contentEquals(decryptedBytes),
        )
    }

    @Test
    fun encryptDecrypt_withMultipleOperations_succeeds() {
        val testData = List(10) { "Test data $it".toByteArray() }

        testData.forEach { originalBytes ->
            val encryptedBytes = Crypto.encrypt(originalBytes)
            val decryptedBytes = Crypto.decrypt(encryptedBytes)
            assertTrue(
                "Each operation should succeed",
                originalBytes.contentEquals(decryptedBytes),
            )
        }
    }
}
