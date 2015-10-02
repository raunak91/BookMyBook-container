package bookmybook

class User {

    Long id
    String city
    String deviceId
    String name
    String email

    static hasMany = [books: UserBookOwnMapping,
                      bookLent: UserBookLentMapping,
                      bookBorrowed: UserBookLentMapping]

    static mappedBy = [bookLent: 'bookLender',
                       bookBorrowed: 'bookLendee']

    static constraints = {
        deviceId(unique: true)
    }
}
