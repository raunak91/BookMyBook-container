package bookmybook

import grails.converters.JSON

class UserController {

    def bookService

    def index() { }

    def saveUser() {
        def returnMap = [:]
        returnMap.status="FAILURE"

        def deviceId = params.DEVICE_ID
        def name = params.NAME
        def email = params.EMAIL
        def city = params.CITY
        def mobile = params.mobile

        if (!deviceId || !email || !mobile) {
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        if(User.findByDeviceId(deviceId)) {
            returnMap.reason = "DeviceId Already Registered"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        } else if(User.findByEmail(email)) {
            returnMap.reason = "Email Already Registered"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        } else {
            new User(name: name, email: email, deviceId: deviceId, city: city).save(flush:true, failOnError: true)
        }

        returnMap.status=" User Successfully Registered"
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def getMyBooks() {
        def returnMap = [:]
        returnMap.status = "FAILURE"

        String deviceId = params.DEVICE_ID

        def user = User.findByDeviceId(deviceId)
        if(user) {
            def booksIds = UserBookOwnMapping.findAllByUserId(user.id).collect {it.bookId}
            def books = Book.findAllByIdInList(booksIds)
            //TODO : Add details regarding whether bookLent/Open to lending and stuff
            returnMap = [status: "SUCCESS", books: books]
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        } else {
            returnMap.value = "USER not found"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        }
    }

    def findUser() {
        def returnMap = [:]
        returnMap.status="FAILURE"

        String deviceId = params.DEVICE_ID
        String email = params.EMAIL_ID

        if (!deviceId || !email) {
            returnMap.reason = "Provide Device Id or Email"
            return render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        }

        def user = findUser(deviceId, email)
        if(!user) {
            returnMap.reason = "User not found"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        } else {
            returnMap.user = user
            returnMap.status="SUCCESS"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        }
    }

    def findUser(String deviceId, String email) {
        if(deviceId) {
            return findUserByDeviceId(deviceId)
        } else {
            return findUserByEmail(email)
        }
    }

    private def findUserByDeviceId(String deviceId) {
        return User.findByDeviceId(deviceId)
    }

    private def findUserByEmail(String email) {
        return User.findByEmail(email)
    }

    def addBookToUser() {
        def returnMap = [:]
        returnMap.status="FAILURE"

        String deviceId = params.DEVICE_ID

        if (!deviceId) {
            returnMap.reason = "Provide Device Id"
            return render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        }

        def user = findUser(deviceId, null)
        if(user) {
            String bookName = params.BOOK_NAME
            String bookAuthor = params.BOOK_AUTHOR

            if(!bookAuthor || !bookName) {
                returnMap.reason = "Incomplete Details!! Book Author or Name empty"
                return render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            }

            Book book = Book.findByAuthorAndName(bookAuthor, bookName)
            if(!book) {
                book = bookService.addBook(bookName, bookAuthor)
                if(!book) {
                    returnMap.reason = "Error Saving Book"
                    return render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
                }
            }

            new UserBookOwnMapping(userId: user.id, bookId: book.id).save(flush: true)
            book.count ++
            book.save(flush: true)

            returnMap.status = "SUCCESS"
        } else {
            returnMap.reason = "User not found"
            return render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        }

        return render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def borrowBook() {
        def returnMap = [:]
        returnMap.status="FAILURE"

        def deviceId = params.DEVICE_ID
        def bookName = params.BOOK_NAME
        def bookAuthor = params.BOOK_AUTHOR
        def city = params.CITY
        def bookOwnerId = params.OWNER

        if(!deviceId || !bookAuthor || !bookName) {
            returnMap.value = "Book Author or Name empty"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }
        def user = User.findByDeviceId(deviceId)
        if(!user) {
            returnMap.value = "USER not found"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def availableBooks = bookService.fetchAvailableBooks(bookAuthor, bookName, city)
        def bookWanted = availableBooks.find{it.userId == bookOwnerId}
        if(bookWanted) {
            lendBook(user, bookWanted)
        } else {
            lendBook(user, availableBooks.get(0))
        }

        returnMap.status = "SUCCESS"
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    private def lendBook(lendee, book) {
        def lender = User.get(book.userId)
    }
}
