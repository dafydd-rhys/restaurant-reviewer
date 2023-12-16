package com.example.mobileapps_cw3.structures

class SystemData {

    companion object {

        private var loggedInUser: Profile? = null
        private var guest: Boolean = false

        fun setGuest (b: Boolean) {
            guest = b
        }

        fun getGuest(): Boolean {
            return guest
        }

        fun setLoggedIn(user: Profile?) {
            this.loggedInUser = user
        }

        fun getLoggedIn(): Profile? {
            return loggedInUser
        }

        fun clear() {
            loggedInUser = null
            guest = false
        }
    }

}