package com.example.mobileapps_cw3.structures


import java.io.Serializable

data class MenuItem(
    private var id: Int = 0,
    private var name: String = "",
    private var cost: Float = 0f
): Serializable {

    fun getId(): Int {
        return this.id
    }

    fun getName(): String {
        return this.name
    }

    fun getCost(): Float {
        return this.cost
    }

}