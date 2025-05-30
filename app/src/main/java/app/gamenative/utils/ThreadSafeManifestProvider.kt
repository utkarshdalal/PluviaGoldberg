// utils/ThreadSafeManifestProvider.kt
package app.gamenative

import `in`.dragonbra.javasteam.steam.contentdownloader.FileManifestProvider
import `in`.dragonbra.javasteam.steam.contentdownloader.IManifestProvider
import `in`.dragonbra.javasteam.types.DepotManifest
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ThreadSafeManifestProvider(file: Path) : IManifestProvider {

    private val delegate = FileManifestProvider(file)
    private val lock = ReentrantLock()

    override fun fetchManifest(depotID: Int, manifestID: Long): DepotManifest? =
        lock.withLock { delegate.fetchManifest(depotID, manifestID) }

    override fun fetchLatestManifest(depotID: Int): DepotManifest? =
        lock.withLock { delegate.fetchLatestManifest(depotID) }

    override fun setLatestManifestId(depotID: Int, manifestID: Long) =
        lock.withLock { delegate.setLatestManifestId(depotID, manifestID) }

    override fun updateManifest(manifest: DepotManifest) =
        lock.withLock { delegate.updateManifest(manifest) }
}
