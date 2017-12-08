package it.oraclize.cordapi.entities

class ProofType {
    companion object {
        val NONE = 0x00
        val TLSNOTARY = 0x10
        val ANDROID = 0x20
        val LEDGER = 0x30
        val NATIVE = 0xF0
    }

}