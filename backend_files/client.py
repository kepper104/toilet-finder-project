import requests as r

BASE_URL = "http://79.120.9.3:5010/"


class User:
    def __init__(self, login, password, display_name):
        self.login = login
        self.password = password
        self.display_name = display_name


class Toilet:
    def __init__(self, author_id, coordinates, place_name, is_public, disabled_access, baby_access, parking_nearby,
                 creation_date, opening_time, closing_time, cost):
        self.author_id = author_id
        self.coordinates = coordinates
        self.place_name = place_name
        self.is_public = is_public
        self.disabled_access = disabled_access
        self.baby_access = baby_access
        self.parking_nearby = parking_nearby
        self.creation_date = creation_date
        self.opening_time = opening_time
        self.closing_time = closing_time
        self.cost = cost

class Review:
    def __init__(self, toilet_id, user_id, stars_number, review_text):
        self.toilet_id = toilet_id
        self.user_id = user_id
        self.stars_number = stars_number
        self.review_text = review_text

class Verification:
    def __init__(self, toilet_id, user_id, vote):
        self.toilet_id = toilet_id
        self.user_id = user_id
        self.vote = vote
def test():
    res = r.get(BASE_URL)
    if res.status_code != 200:
        print(f"Error: {res.status_code}, content: {res.content}")
        exit()

    try:
        res_json = res.json()
        print(res_json)
    except:
        print("Json Error, printing contents")
        print(res.content)


def get_users():
    res = r.get(BASE_URL + "users")
    print(res.content)


def add_user(user: User):
    json = {
        "login": user.login,
        "password": user.password,
        "display_name": user.display_name
    }
    res = r.post(BASE_URL + "users", json=json)
    print(res, res.text)


def check_if_user_exists(user_login: str):
    res = r.get(BASE_URL + f"user_exists/{user_login}")
    print(res)


def add_toilet(toilet: Toilet):
    json = {
        "author_id": toilet.author_id,
        "coordinates": toilet.coordinates,
        "place_name": toilet.place_name,
        "is_public": toilet.is_public,
        "disabled_access": toilet.disabled_access,
        "baby_access": toilet.baby_access,
        "parking_nearby": toilet.parking_nearby,
        "creation_date": toilet.creation_date,
        "opening_time": toilet.opening_time,
        "closing_time": toilet.closing_time,
        "cost": toilet.cost
    }
    res = r.post(BASE_URL + "toilets", json=json)
    print(res, res.text)


def check_password(login, password):
    json = {
        "login": login,
        "password": password
    }
    res = r.post(BASE_URL + 'users/login', json=json)
    print(res, res.text, res.status_code)


def add_review(review: Review):
    json = {
        "toilet_id": review.toilet_id,
        "user_id": review.user_id,
        "stars_number": review.stars_number,
        "review_text": review.review_text,
    }

    res = r.post(BASE_URL + 'reviews', json=json)
    print(res, res.text, res.status_code)


def add_verification(verification: Verification):
    json = {
        "toilet_id": verification.toilet_id,
        "user_id": verification.user_id,
        "vote": verification.vote,
    }

    res = r.post(BASE_URL + 'verifications', json=json)
    print(res, res.text, res.status_code)


def send_report(user_id, toilet_id, message):
    json = {
        "toilet_id_": toilet_id,
        "user_id_": user_id,
        "message_": message,
    }

    res = r.post(BASE_URL + 'toilets/report', json=json)
    print(res, res.text, res.status_code)


# get_users()
# add_user(User("kepper", "pass_test", "fedya"))
# add_toilet(Toilet(1, (55.69, 37.56), 'Home toilet', False, False, False, True, None, '01:00:00', '23:00:00', 999))

# check_password("kepper104", "pass1d23")
# add_review(Review(3, 6, 5, "Best toilet i've ever visited"))

# add_verification(Verification(1, 2, -1))

send_report(2, 3, "doesnt exist")