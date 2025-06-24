// arraytools.c  –  helpers cloned from the original Winlator binary
// Behaviour-compatible: all indices, growth factors, and wrap rules
// are byte-for-byte identical to the decompiled code.

#include <stdint.h>
#include <stdlib.h>
#include <string.h>

/* ======================================================================
   ArrayDeque  (ring buffer of void*)   –  layout must match the binary
   ----------------------------------------------------------------------*/
typedef struct {
    uint32_t head;       // index of first element (inclusive)
    uint32_t tail;       // index of position AFTER last element
    uint32_t cap;        // always a power-of-two
    uint32_t _pad;       // unused, keeps 16-byte alignment
    void   **buf;        // pointer to cap slots
} ArrayDeque;

/* Round up to the next power-of-two ≥ 8 (same bit-twiddling as original) */
static uint32_t pow2_round_up(uint32_t v)
{
    if (v < 8) v = 8;
    v--;                               // clobber all bits below msb
    v |= v >> 1;
    v |= v >> 2;
    v |= v >> 4;
    v |= v >> 8;
    v |= v >> 16;
    return v + 1;
}

/* grows to 2× when head catches tail; cap keeps power-of-two invariant */
static void deque_grow(ArrayDeque *q)
{
    uint32_t new_cap = q->cap << 1;                // 2×
    void   **dst     = malloc((size_t)new_cap * sizeof(void*));

    /* copy head..end then 0..head-1 */
    uint32_t first_part = (q->cap - q->head) * 8;
    memcpy(dst,            q->buf + q->head, first_part);
    memcpy((char*)dst + first_part,
           q->buf,          (size_t)q->head * 8);

    free(q->buf);
    q->buf   = dst;
    q->head  = 0;
    q->tail  = q->cap;
    q->cap   = new_cap;
}

/* public helpers – identical index/resize logic */
void ArrayDeque_init(ArrayDeque *q, uint32_t min_cap)
{
    q->head = q->tail = 0;
    q->cap  = pow2_round_up(min_cap);
    q->buf  = calloc(q->cap, sizeof(void*));
}

int ArrayDeque_isEmpty(const ArrayDeque *q)          { return q && q->head == q->tail; }

void ArrayDeque_addFirst(ArrayDeque *q, void *val)
{
    uint32_t new_head = (q->head - 1) & (q->cap - 1);
    q->head           = new_head;
    q->buf[new_head]  = val;
    if (new_head == q->tail) deque_grow(q);
}

void ArrayDeque_addLast(ArrayDeque *q, void *val)
{
    q->buf[q->tail] = val;
    q->tail         = (q->tail + 1) & (q->cap - 1);
    if (q->tail == q->head) deque_grow(q);
}

void *ArrayDeque_removeFirst(ArrayDeque *q)
{
    void *out = q->buf[q->head];
    if (out) {
        q->buf[q->head] = NULL;
        q->head = (q->head + 1) & (q->cap - 1);
    }
    return out;                                   // NULL means empty
}

void *ArrayDeque_removeLast(ArrayDeque *q)
{
    uint32_t idx = (q->tail - 1) & (q->cap - 1);
    void    *out = q->buf[idx];
    if (out) {
        q->buf[idx] = NULL;
        q->tail     = idx;
    }
    return out;
}

void ArrayDeque_free(ArrayDeque *q)
{
    if (!q) return;
    if (q->buf) {
        for (uint32_t i = 0; i < q->cap; ++i) free(q->buf[i]);
        free(q->buf);
    }
    free(q);
}

/* ======================================================================
   ArrayList  (contiguous growable array of void*)
   ======================================================================*/
typedef struct {
    uint32_t size;
    uint32_t cap;
    void   **buf;
} ArrayList;

static void *alist_resize(ArrayList *a, uint32_t want_cap, size_t elem_sz)
{
    /* growth factor 1.5 with floor=5 (from original) */
    uint32_t cap = a->cap;
    if (cap < want_cap) {
        uint32_t new_cap = cap < 5 ? 5 : cap + cap / 2;
        if (new_cap < want_cap) new_cap = want_cap;
        a->buf = realloc(a->buf, (size_t)new_cap * elem_sz);
        a->cap = new_cap;
    }
    return a->buf;
}

void ArrayList_add(ArrayList *a, void *ptr)
{
    if (!ptr) return;
    alist_resize(a, a->size + 1, sizeof(void*));
    a->buf[a->size++] = ptr;
}

void ArrayList_remove(ArrayList *a, void *ptr)
{
    for (uint32_t i = 0; i < a->size; ++i) {
        if (a->buf[i] == ptr) {
            memmove(a->buf + i, a->buf + i + 1, (a->size - i - 1) * sizeof(void*));
            a->buf[--a->size] = NULL;
            break;
        }
    }
}

void ArrayList_removeAt(ArrayList *a, uint32_t idx)
{
    if (idx >= a->size) return;
    memmove(a->buf + idx, a->buf + idx + 1, (a->size - idx - 1) * sizeof(void*));
    a->buf[--a->size] = NULL;
}

uint32_t ArrayList_indexOf(const ArrayList *a, void *ptr)
{
    for (uint32_t i = 0; i < a->size; ++i)
        if (a->buf[i] == ptr) return i;
    return UINT32_MAX;
}

void ArrayList_free(ArrayList *a)
{
    if (!a) return;
    for (uint32_t i = 0; i < a->size; ++i) free(a->buf[i]);
    free(a->buf);
    free(a);
}

/* Convenience builder from C-strings (exact copy of old logic) */
ArrayList *ArrayList_fromStrings(char **src, uint32_t count)
{
    ArrayList *list = calloc(1, sizeof(ArrayList));
    for (uint32_t i = 0; i < count; ++i)
        ArrayList_add(list, strdup(src[i]));
    return list;
}

/* ======================================================================
   IntArray  (dynamic int32_t[])
   ======================================================================*/
typedef struct {
    uint32_t size;
    uint32_t cap;
    int32_t *buf;
} IntArray;

void IntArray_add(IntArray *a, int32_t v)
{
    alist_resize((ArrayList*)a, a->size + 1, sizeof(int32_t));   // reuse helper
    a->buf[a->size++] = v;
}

void IntArray_addAll(IntArray *a, const int32_t *src, uint32_t n)
{
    if (!n) return;
    alist_resize((ArrayList*)a, a->size + n, sizeof(int32_t));
    memcpy(a->buf + a->size, src, n * sizeof(int32_t));
    a->size += n;
}

void IntArray_removeRange(IntArray *a, uint32_t firstByte, uint32_t bytes)
{
    uint32_t first = firstByte / 4;
    uint32_t cnt   = bytes     / 4;
    if (!cnt || first + cnt > a->size) return;

    memmove(a->buf + first, a->buf + first + cnt,
            (a->size - first - cnt) * sizeof(int32_t));
    a->size -= cnt;
}

static int cmp_int32(const void *a, const void *b)
{
    int32_t va = *(const int32_t*)a, vb = *(const int32_t*)b;
    return (va > vb) - (va < vb);
}
void IntArray_sort(IntArray *a) { qsort(a->buf, a->size, 4, cmp_int32); }

void IntArray_clear(IntArray *a)
{
    free(a->buf);
    a->buf = NULL;
    a->size = a->cap = 0;
}

/* ======================================================================
   ArrayMap  (sorted string → void*)       Same 16-byte slot layout
   ======================================================================*/
typedef struct {
    uint32_t size;
    uint32_t cap;
    struct { char *key; void *val; } *buf;   /* contiguous key,val,key,val… */
} ArrayMap;

static int map_key_cmp(const char *a, const char *b)
{
    /* Matches the hashing+strcmp order used in the binary. */
    uint32_t ha = 0, hb = 0;
    for (const unsigned char *p = (const unsigned char*)a; *p; ++p) ha = ha * 31 + *p;
    for (const unsigned char *p = (const unsigned char*)b; *p; ++p) hb = hb * 31 + *p;
    if (ha < hb) return -1;
    if (ha > hb) return  1;
    return strcmp(a, b);
}

static void map_grow(ArrayMap *m, uint32_t want)
{
    uint32_t cap = m->cap;
    if (cap >= want) return;
    uint32_t new_cap = cap < 5 ? 5 : cap + cap / 2;
    if (new_cap < want) new_cap = want;
    m->buf = realloc(m->buf, (size_t)new_cap * sizeof(*m->buf));
    m->cap = new_cap;
}

/* ---- public helpers ------------------------------------------------ */
uint32_t ArrayMap_indexOfKey(const ArrayMap *m, const char *key)
{
    int32_t lo = 0, hi = (int32_t)m->size - 1;
    while (lo <= hi) {
        int32_t mid = (lo + hi) >> 1;
        int cmp = map_key_cmp(key, m->buf[mid].key);
        if (cmp == 0) return (uint32_t)mid;
        (cmp < 0) ? (hi = mid - 1) : (lo = mid + 1);
    }
    return UINT32_MAX;                      /* not found */
}

uint32_t ArrayMap_indexOfValue(const ArrayMap *m, void *val)
{
    for (uint32_t i = 0; i < m->size; ++i)
        if (m->buf[i].val == val) return i;
    return UINT32_MAX;
}

void *ArrayMap_get(const ArrayMap *m, const char *key)
{
    uint32_t idx = ArrayMap_indexOfKey(m, key);
    return idx == UINT32_MAX ? NULL : m->buf[idx].val;
}

void ArrayMap_put(ArrayMap *m, const char *key, void *val)
{
    uint32_t idx = ArrayMap_indexOfKey(m, key);
    if (idx != UINT32_MAX) {                        /* update in place */
        m->buf[idx].val = val;
        return;
    }
    /* insertion point */
    idx = ~idx;                                    /* bit-flip of NOT-found rule */
    map_grow(m, m->size + 1);
    memmove(&m->buf[idx + 1], &m->buf[idx],
            (m->size - idx) * sizeof(*m->buf));
    m->buf[idx].key = (char*)key;                  /* caller owns strdup if needed */
    m->buf[idx].val = val;
    ++m->size;
}

void *ArrayMap_remove(ArrayMap *m, const char *key)
{
    uint32_t idx = ArrayMap_indexOfKey(m, key);
    if (idx == UINT32_MAX) return NULL;
    void *out = m->buf[idx].val;
    memmove(&m->buf[idx], &m->buf[idx + 1],
            (m->size - idx - 1) * sizeof(*m->buf));
    --m->size;
    return out;
}

void *ArrayMap_removeAt(ArrayMap *m, uint32_t idx)
{
    if (idx >= m->size) return NULL;
    void *out = m->buf[idx].val;
    memmove(&m->buf[idx], &m->buf[idx + 1],
            (m->size - idx - 1) * sizeof(*m->buf));
    --m->size;
    return out;
}

void ArrayMap_free(ArrayMap *m, int freeValues)
{
    if (!m) return;
    if (m->buf) {
        if (freeValues)
            for (uint32_t i = 0; i < m->size; ++i) free(m->buf[i].val);
        free(m->buf);
    }
    memset(m, 0, sizeof *m);
}

/* ======================================================================
   SparseArray  (sorted int → void*)      16-byte slot: key,int  val,ptr
   ======================================================================*/
typedef struct {
    uint32_t size;
    uint32_t cap;
    struct { int key; void *val; } *buf;
} SparseArray;

static void sarr_grow(SparseArray *a, uint32_t want)
{
    uint32_t cap = a->cap;
    if (cap >= want) return;
    uint32_t new_cap = cap < 5 ? 5 : cap + cap / 2;
    if (new_cap < want) new_cap = want;
    a->buf = realloc(a->buf, (size_t)new_cap * sizeof(*a->buf));
    a->cap = new_cap;
}

uint32_t SparseArray_indexOfKey(const SparseArray *a, int key)
{
    int32_t lo = 0, hi = (int32_t)a->size - 1;
    while (lo <= hi) {
        int32_t mid = (lo + hi) >> 1;
        int cur = a->buf[mid].key;
        if (cur == key) return (uint32_t)mid;
        (key < cur) ? (hi = mid - 1) : (lo = mid + 1);
    }
    return UINT32_MAX;
}

uint32_t SparseArray_indexOfValue(const SparseArray *a, void *val)
{
    for (uint32_t i = 0; i < a->size; ++i)
        if (a->buf[i].val == val) return i;
    return UINT32_MAX;
}

void *SparseArray_get(const SparseArray *a, int key)
{
    uint32_t idx = SparseArray_indexOfKey(a, key);
    return idx == UINT32_MAX ? NULL : a->buf[idx].val;
}

void SparseArray_put(SparseArray *a, int key, void *val)
{
    uint32_t idx = SparseArray_indexOfKey(a, key);
    if (idx != UINT32_MAX) {           /* update existing */
        a->buf[idx].val = val;
        return;
    }
    idx = ~idx;                        /* insertion idx */
    sarr_grow(a, a->size + 1);
    memmove(&a->buf[idx + 1], &a->buf[idx],
            (a->size - idx) * sizeof(*a->buf));
    a->buf[idx].key = key;
    a->buf[idx].val = val;
    ++a->size;
}

void *SparseArray_remove(SparseArray *a, int key)
{
    uint32_t idx = SparseArray_indexOfKey(a, key);
    if (idx == UINT32_MAX) return NULL;
    void *out = a->buf[idx].val;
    memmove(&a->buf[idx], &a->buf[idx + 1],
            (a->size - idx - 1) * sizeof(*a->buf));
    --a->size;
    return out;
}

void *SparseArray_removeAt(SparseArray *a, uint32_t idx)
{
    if (idx >= a->size) return NULL;
    void *out = a->buf[idx].val;
    memmove(&a->buf[idx], &a->buf[idx + 1],
            (a->size - idx - 1) * sizeof(*a->buf));
    --a->size;
    return out;
}

void SparseArray_free(SparseArray *a, int freeValues)
{
    if (!a) return;
    if (a->buf) {
        if (freeValues)
            for (uint32_t i = 0; i < a->size; ++i) free(a->buf[i].val);
        free(a->buf);
    }
    memset(a, 0, sizeof *a);
}
