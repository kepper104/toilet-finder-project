from db_connector import DBManager
from flask import Flask, request, Response
from config import *
import json
import datetime
class Result:
    def __init__(self, status: int, message):
        self.status = status
        self.message = message

    def display(self):
        return Response(json.dumps(self.message, default=str), self.status, mimetype='application/json')


app = Flask(__name__)
app.config['JSON_SORT_KEYS'] = False

db = DBManager(host, username, password, database_name)


@app.get('/')
def index():
    print("Sending server status...")

    try:
        return Result(200, {"Status": "Server online"}).display()
    except Exception as e:
        return Result(500, {'Status': "Server error: " + str(e)}).display()


@app.get('/users')
def get_all_users():
    print("Getting all users...")

    users = db.get_all_users()

    return Result(200, users).display()


@app.get('/users/<id>')
def get_user_by_id(id: int):
    print("Getting user by id...")

    user = db.get_user_by_id(id)

    user.pop("login_", None)
    user.pop("password_hashed_", None)

    if user:
        return Result(200, user).display()
    else:
        return Result(404, {"Status": "Not found"}).display()


@app.post('/users')
def add_user():
    print("Adding user...")

    insertion_res = db.add_user(request.json)
    if not insertion_res:
        return Result(201, {"Message": "User created"}).display()
    else:
        return Result(500, {"Message": str(insertion_res)}).display()


@app.get('/user_exists/<login>')
def check_if_user_exists(login: str):
    print("Getting login availability...")

    res = db.check_if_user_exists(login)

    return Result(200, {"UserExists": res}).display()


@app.get('/toilets')
def get_all_toilets():
    print("Getting all toilets...")

    toilets = db.get_all_toilets()

    for toilet in toilets:
        avg = 0
        user_id = toilet["author_id_"]
        user = db.get_user_by_id(user_id)
        name = user["display_name_"]

        reviews = db.get_reviews_by_toilet_id(toilet["id_"])
        count = 0
        if reviews:

            for review in reviews:
                rating = review["rating_"]
                avg += rating
                count += 1
        if count:
            avg = avg / count
        toilet["average_rating_"] = avg
        toilet["author_name_"] = name
        toilet["review_count_"] = count

    return Result(200, toilets).display()


@app.get('/toilets/<id>')
def get_toilet_by_id(id: int):
    print("Getting toilet by id...")

    toilet = db.get_toilet(id)

    avg = 0
    user_id = toilet["author_id_"]
    user = db.get_user_by_id(user_id)
    name = user["display_name_"]

    reviews = db.get_reviews_by_toilet_id(toilet["id_"])
    count = 0
    if reviews:

        for review in reviews:
            rating = review["rating_"]
            avg += rating
            count += 1

    if count:
        avg = avg / count

    toilet["average_rating_"] = avg
    toilet["author_name_"] = name
    toilet["review_count_"] = count


    if toilet:
        return Result(200, toilet).display()
    else:
        return Result(404, {"Message": "Toilet not found!"}).display()


@app.post('/toilets')
def add_toilet():
    print("Adding toilet...")

    insertion_res = db.add_toilet(request.json)
    print(insertion_res)
    if not insertion_res:
        return Result(201, {"Message": "Toilet created"}).display()
    else:
        return Result(500, {"Message": str(insertion_res)}).display()


@app.post('/users/login')
def check_login_and_password():
    print("Checking password...")

    res = db.check_password(request.json)

    if res:
        res.pop("login_", None)
        res.pop("password_hashed_", None)
        return Result(200, res).display()
    else:
        return Result(404, {}).display()


@app.get('/reviews')
def get_all_reviews():
    print("Getting all reviews...")

    reviews = db.get_all_reviews()
    for review in reviews:
        user_id = review["user_id_"]
        user = db.get_user_by_id(user_id)
        name = user["display_name_"]

        review["user_display_name_"] = name
        print(review)

    return Result(200, reviews).display()


@app.get('/reviews/<id>')
def get_reviews_by_toilet_id(id: int):
    print("Getting reviews by toilet id...")

    reviews = db.get_reviews_by_toilet_id(id)

    if reviews:
        for review in reviews:
            user_id = review["user_id_"]
            user = db.get_user_by_id(user_id)
            name = user["display_name_"]

            review["user_display_name_"] = name

        return Result(200, reviews).display()
    else:
        return Result(404, {"Message": "Reviews not found!"}).display()


@app.post('/reviews')
def add_review():
    print("Adding review...")

    insertion_res = db.add_review(request.json)
    if not insertion_res:
        return Result(201, {"Message": "Review created"}).display()
    else:
        return Result(500, {"Message": str(insertion_res)}).display()


@app.get('/verifications')
def get_all_verifications():
    print("Getting all verifications...")

    verifications = db.get_all_verifications()

    return Result(200, verifications).display()


@app.get('/verifications/<id>')
def get_verifications_by_id(id: int):
    print("Getting verifications by id...")

    verification = db.get_verification(id)
    if verification:
        return Result(200, verification).display()
    else:
        return Result(404, {"Message": "Verification not found!"}).display()


@app.post('/verifications')
def add_verification():
    print("Adding verification...")

    insertion_res = db.add_verification(request.json)
    if not insertion_res:
        return Result(201, {"Message": "Verification created"}).display()
    else:
        return Result(500, {"Message": str(insertion_res)}).display()


@app.post('/users/change_name')
def change_display_name():
    print("Changing name")

    update_res = db.change_name(request.json)

    if not update_res:
        return Result(200, {"Message": "Display Name updated"}).display()
    else:
        return Result(500, {"Message": set(update_res)}).display()


@app.post("/toilets/report")
def report_toilet():
    report_data = request.json

    try:
        user_id = report_data["user_id_"]
        toilet_id = report_data["toilet_id_"]
        message = report_data["message_"]

        res_string = f"{datetime.datetime.now().__str__()} - UserID {user_id} ToiletID {toilet_id} Message {message} \n"
        with open("report_log.txt", "a") as f:
            f.write(res_string)
        return Result(200, {"Message": "New report added"}).display()
    except Exception as e:
        return Result(500, {"Message": str(e)}).display()




if __name__ == '__main__':
    app.run()
