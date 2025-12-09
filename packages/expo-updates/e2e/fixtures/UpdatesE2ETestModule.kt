package expo.modules.updates

import android.os.Bundle
import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.types.Enumerable
import expo.modules.updatesinterface.UpdatesControllerRegistry
import expo.modules.updatesinterface.UpdatesEnabledInterface
import expo.modules.updatesinterface.UpdatesStateChangeListener
import expo.modules.updatesinterface.statemachine.UpdatesStateEvent

interface UpdatesEnabledTestingInterface: UpdatesEnabledInterface {
  fun clearInternalAssetsFolderAsync(promise: Promise)
  fun readInternalAssetsFolderAsync(promise: Promise)
}

class UpdatesE2ETestModule : Module(), UpdatesStateChangeListener {
  private var hasListener: Boolean = false

  override fun definition() = ModuleDefinition {
    Name("ExpoUpdatesE2ETest")

    Events<UpdatesE2EEvent>()

    OnStartObserving(UpdatesE2EEvent.StateChange) {
      hasListener = true
    }

    OnStopObserving(UpdatesE2EEvent.StateChange) {
      hasListener = false
    }

    AsyncFunction("clearInternalAssetsFolderAsync") { promise: Promise ->
      UpdatesControllerRegistry.controller?.get()?.let {
        if (it is UpdatesEnabledTestingInterface) {
          it.clearInternalAssetsFolderAsync(promise)
        }
      }
    }

    AsyncFunction("readInternalAssetsFolderAsync") { promise: Promise ->
      UpdatesControllerRegistry.controller?.get()?.let {
        if (it is UpdatesEnabledTestingInterface) {
          it.readInternalAssetsFolderAsync(promise)
        }
      }
    }
  }

  override fun updatesStateDidChange(event: UpdatesStateEvent) {
    if (hasListener) {
      val payload = Bundle()
      payload.putString("type", event.type.toString())
      when(event) {
        is UpdatesStateEvent.CheckCompleteWithUpdate -> {
          val manifest = event.manifest
          val manifestBundle = Bundle()
          manifestBundle.putString("id", manifest.getString("id"))
          payload.putBundle("manifest", manifestBundle)
        }
        is UpdatesStateEvent.DownloadCompleteWithUpdate -> {
          val manifest = event.manifest
          val manifestBundle = Bundle()
          manifestBundle.putString("id", manifest.getString("id"))
          payload.putBundle("manifest", manifestBundle)
        }
        else -> {}
      }
      sendEvent(E2E_EVENT_NAME, payload)
    }
  }

  companion object {
    const val E2E_EVENT_NAME = "Expo.updatesE2EStateChangeEvent"
  }
}

enum class UpdatesE2EEvent(val eventName: String) : Enumerable {
  StateChange(UpdatesE2ETestModule.E2E_EVENT_NAME)
}
