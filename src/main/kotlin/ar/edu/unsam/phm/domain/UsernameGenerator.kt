package ar.edu.unsam.phm.domain

object UsernameGenerator {

    /**
     * Generates a unique username from the suggested one.
     * Receives a function to check existence — avoids direct dependency on the repository.
     * If "ana_garcia" exists, tries "ana_garcia_2", "ana_garcia_3", etc.
     */
    fun generate(suggested: String, usernameExists: (String) -> Boolean): String {
        if (!usernameExists(suggested)) return suggested
        var counter = 2
        while (usernameExists("${suggested}_$counter")) counter++
        return "${suggested}_$counter"
    }
}
