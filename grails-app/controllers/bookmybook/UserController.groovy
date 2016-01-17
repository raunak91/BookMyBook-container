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

        def deviceId = params.DEVICE_ID

        def user = User.findByDeviceId(deviceId)
        if(user) {
            def booksIds = UserBookOwnMapping.findByUserId(user.id).collect {it.bookId}
            def books = Book.findAllByIdInList(booksIds)
            returnMap = [status: "SUCCESS", books: books]
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        } else {
            returnMap.value = "USER not found"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        }
    }

    def findUser() {
        String deviceId = params.DEVICE_ID
        String email = params.EMAIL_ID
        return findUser(deviceId, email)
    }

    def findUser(String deviceId, String email) {
        if(deviceId) {
            return findUserByDeviceId(deviceId)
        } else {
            return findUserByEmail(email)
        }
    }

    def findUserByDeviceId(String deviceId) {
        def returnMap = [:]
        returnMap.status="FAILURE"

        if (!deviceId) {
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def user = User.findByDeviceId(deviceId)
        if(!user) {
            returnMap.reason = "Device not Registered"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        } else {
            returnMap.user = user
            returnMap.status="SUCCESS"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        }
    }

    def findUserByEmail(String email) {
        def returnMap = [:]
        returnMap.status="FAILURE"

        if (!email) {
            returnMap.reason = "Email not provided"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def user = User.findByEmail(email)
        if(!user) {
            returnMap.reason = "Email not Registered"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        } else {
            returnMap.user = user
            returnMap.status="SUCCESS"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        }
    }

    def addBookToUser() {
        def returnMap = [:]
        returnMap.status="FAILURE"

        String deviceId = params.DEVICE_ID
        String email = params.EMAIL_ID

        def user = findUser(deviceId, email)
        if(user.user) {
            String bookName = params.BOOK_NAME
            String bookAuthor = params.BOOK_AUTHOR

            if(!bookAuthor || !bookName) {
                returnMap.value = "Incomplete Details!! Book Author or Name empty"
                render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
                return
            }

            def book = Book.findByAuthorAndName(bookAuthor, bookName)
            if(book) {
                new UserBookOwnMapping(userId: user.user.id, bookId: book.id).save(flush: true)
                book.count ++
                book.save(flush: true)
            } else {
                bookService.addBook(bookName, bookAuthor)
            }
        }

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
