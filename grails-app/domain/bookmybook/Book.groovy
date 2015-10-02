package bookmybook

class Book {

    Integer id
    String name
    String author
    Integer count
    List<String> imageUrl

    static hasMany = [users: UserBookOwnMapping,
                      bookLent: UserBookLentMapping]

    static mappedBy = [users: 'book',
                       bookLent: 'book']

    static constraints = {

    }

}
