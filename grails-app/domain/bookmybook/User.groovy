package bookmybook

class User {

    Long id
    String city
    String deviceId
    String name
    String email

    static hasMany = [books: Book]

    static constraints = {
        deviceId(unique: true)
    }
}
