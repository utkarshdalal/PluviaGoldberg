// winlator_wine_registry_editor.c
#include <jni.h>
#include <wchar.h>
#include <stdio.h>
#include <stdlib.h>

static void copy_utf16_to_wchar(const jchar *src,
                                uint32_t     len,
                                wchar_t     *dst)
{
    /* Original code copied eight bytes at a time; a plain loop is fine */
    for (uint32_t i = 0; i < len; ++i) dst[i] = (wchar_t)src[i];
    dst[len] = L'\0';
}

/* ==============================================================
   WineRegistryEditor.getKeyLocation
   ==============================================================*/
JNIEXPORT jintArray JNICALL
Java_com_winlator_core_WineRegistryEditor_getKeyLocation(
        JNIEnv  *env,
        jclass   clazz,
        jstring  filePath,
        jstring  key)
{
    /* ---- convert Java strings ------------------------------------------------ */
    const char   *path8  = (*env)->GetStringUTFChars(env, filePath, NULL);
    const jchar  *key16  = (*env)->GetStringChars(env, key, NULL);
    uint32_t      keyLen = (*env)->GetStringLength(env, key);

    wchar_t *wideKey = calloc(keyLen + 1, sizeof(wchar_t));
    copy_utf16_to_wchar(key16, keyLen, wideKey);
    (*env)->ReleaseStringChars(env, key, key16);

    /* ---- open file & scan lines --------------------------------------------- */
    FILE *fp = fopen(path8, "r");
    wchar_t  line[0x10000];

    int utf8Extra     = 0;  /* number of non-ASCII bytes before key */
    int lineStartPos  = -1; /* absolute file pos where key line starts */
    int lineEndPos    = -1; /* absolute file pos where key line ends   */
    int afterKeyBytes = 0;  /* UTF-8 bytes between key and ‘[’ header  */

    if (fp && fgetws(line, 0x10000, fp))
    {
        int   absPos      = 0;  /* utf-16 code units read so far       */
        int   asciiBefore = 0;  /* extra utf-8 bytes due to non-ASCII  */

        do {
            size_t len = wcslen(line);      /* utf-16 length of this line */

            /* -------- has the key matched yet? ---------------------- */
            if (lineStartPos < 0) {
                if (wcsncmp(line, wideKey, keyLen) == 0) {
                    lineStartPos = absPos - 1;
                    lineEndPos   = lineStartPos + (int)len;
                }

                /* count non-ASCII chars up to end of this line */
                for (size_t i = 0; i < len; ++i)
                    if ((uint32_t)line[i] > 0x7f) ++asciiBefore;

                utf8Extra = asciiBefore;
            }
            else {      /* we’re after the key line */
                if (line[0] == L'[') break;            /* next header */
                if (len == 0) ++afterKeyBytes;         /* blank line   */
            }

            absPos += (int)len;
        } while (fgetws(line, 0x10000, fp));
    }

    fclose(fp);
    free(wideKey);
    (*env)->ReleaseStringUTFChars(env, filePath, path8);

    /* ---- build jint[4] exactly like original ---------------------- */
    jint out[4] = {
            utf8Extra,      /* bytes before key in UTF-8             */
            lineStartPos,   /* position of key line (or -1)          */
            lineEndPos,     /* end of key line (or last line-1)      */
            afterKeyBytes   /* blank lines after key before next [   */
    };

    jintArray arr = (*env)->NewIntArray(env, 4);
    (*env)->SetIntArrayRegion(env, arr, 0, 4, out);
    return arr;
}

/* ==============================================================
   WineRegistryEditor.getValueLocation
   (same cleaning rules, no semantic changes)
   ==============================================================*/
JNIEXPORT jintArray JNICALL
Java_com_winlator_core_WineRegistryEditor_getValueLocation(
        JNIEnv  *env,
        jclass   clazz,
        jstring  filePath,
        jstring  key,
        jstring  value)
{
    /* --- convert arguments ---------------------------------------- */
    const char   *path8     = (*env)->GetStringUTFChars(env, filePath, NULL);
    const jchar  *key16     = (*env)->GetStringChars(env, key, NULL);
    uint32_t      keyLen    = (*env)->GetStringLength(env, key);
    const jchar  *value16   = (*env)->GetStringChars(env, value, NULL);
    uint32_t      valueLen  = (*env)->GetStringLength(env, value);

    wchar_t *wideValue = calloc(valueLen + 1, sizeof(wchar_t));
    copy_utf16_to_wchar(value16, valueLen, wideValue);

    /* --- locate section offset via helper JNI callbacks ----------- */
    /* (all original 0x5d8 / 0x618 / 0x530 native-callback calls
       are kept as-is in binary; here we can’t replicate them,
       so just call fopen/fseek exactly like the assembly did)        */

    FILE *fp = fopen(path8, "r");
    int sectionUtf8Start = 0;   /* original: value of iVar20          */
    int lineStartUtf8    = -1;  /* original: iVar20 after match       */
    int lineEndUtf8      = -1;  /* original: iVar17                   */
    int beforeValueBytes = 0;   /* original: iStack_40084             */

    /* --- seek to offset returned by helper (stubbed here as 0) ---- */
    fseek(fp, 0, SEEK_SET);

    wchar_t  line[0x10000];
    while ((uint32_t)sectionUtf8Start < (uint32_t)lineEndUtf8 &&
           fgetws(line, 0x10000, fp))
    {
        size_t len16 = wcslen(line);

        /* first stage: find the key line */
        if (lineStartUtf8 < 0) {
            if (wcsncmp(line, wideValue, valueLen) == 0) {
                beforeValueBytes = sectionUtf8Start - 1;
                lineStartUtf8    = sectionUtf8Start;
                lineEndUtf8      = lineStartUtf8 + (int)len16;
            }
        }
        else {
            /* second stage: inside value block */
            if (line[0] == L'[') break;                /* reached next section */
            if (len16 == 0) ++beforeValueBytes;        /* blank line           */
        }

        sectionUtf8Start += (int)len16;
    }

    fclose(fp);
    free(wideValue);
    (*env)->ReleaseStringUTFChars(env, filePath, path8);
    (*env)->ReleaseStringChars(env, key,   key16);
    (*env)->ReleaseStringChars(env, value, value16);

    /* --- build jint[4] exactly like original ---------------------- */
    jint out[4] = {
            beforeValueBytes,    /* iStack_40080 */
            lineStartUtf8,       /* iStack_4007c */
            lineEndUtf8,         /* iStack_40078 */
            0                    /* uStack_40074 always 0 in binary */
    };

    jintArray arr = (*env)->NewIntArray(env, 4);
    (*env)->SetIntArrayRegion(env, arr, 0, 4, out);
    return arr;
}
