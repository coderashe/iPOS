package com.ipos.app.core

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceSession @Inject constructor() {
    var deviceId: String = ""
    var storeId: String = ""
    var deviceName: String = ""

    fun isRegistered() = deviceId.isNotEmpty()
}
