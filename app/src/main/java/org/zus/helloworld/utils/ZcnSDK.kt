package org.zus.helloworld.utils

import com.google.gson.Gson
import zcn.Zcn
import zcncore.Zcncore


class ZcnSDK {
    fun faucet(methodName: String, input: Any, zcnToken: Double): String  {
        return Zcn.faucet(methodName, Gson().toJson(input), zcnToken)
    }

    fun executeSmartContract(address: String, methodName: String, input: Any, sasToken: UInt): String  {
        return Zcn.executeSmartContract(address,methodName, Gson().toJson(input), sasToken.toString())
    }

    fun readPoolLock(tokens: Double, fee : Double) : String {
        return Zcn.readPoolLock(Zcncore.convertToValue(tokens), Zcncore.convertToValue(fee))
    }

    fun writePoolLock(allocID: String, tokens: Double, fee : Double) : String {
        return Zcn.writePoolLock(allocID, Zcncore.convertToValue(tokens), Zcncore.convertToValue(fee))
    }
}
