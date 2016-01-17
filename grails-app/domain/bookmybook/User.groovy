package bookmybook

class User {

    Long id
    String city
    String deviceId
    String name
    String email
    String mobile

    static constraints = {
        deviceId(unique: true)
    }
}
