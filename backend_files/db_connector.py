import psycopg2
from argon2.exceptions import VerifyMismatchError, VerificationError, InvalidHashError
from argon2 import PasswordHasher
from config import password, username, database_name, host
import argon2


class DBManager:
    def __init__(self, db_host: str, db_username: str, db_password: str, db_name: str, db_port: int = 5432):
        self.db_host = db_host
        self.db_name = db_name
        self.db_username = db_username
        self.db_password = db_password
        self.db_port = db_port

        print("Connecting to DB...")

        connector = (f"host={self.db_host} "
                     f"dbname={self.db_name} "
                     f"user={self.db_username} "
                     f"password={self.db_password} "
                     f"port={self.db_port}")

        self.conn = psycopg2.connect(connector)

        print("Connection successful")

        self.cur = self.conn.cursor()

    def get_columns(self, table_name_: str):

        # maybe add this AND table_schema = 'your_schema_name'

        self.cur.execute("SELECT column_name "
                         "FROM information_schema.columns "
                         f"WHERE table_schema = 'public' AND table_name = '{table_name_}';")

        res = self.cur.fetchall()

        columns = list(map(lambda x: x[0], res))

        return columns

    def map_columns_and_data(self, columns, data):
        res = list()

        if type(data) != list:
            data = [data]
        for entry in data:
            element = dict()
            for index, item in enumerate(columns):
                element[item] = entry[index]
            res.append(element)

        return res

    def get_table_contents(self, table_name: str) -> list:
        self.cur.execute(f"SELECT * FROM {table_name}")
        data = self.cur.fetchall()
        columns = self.get_columns(table_name)

        res = self.map_columns_and_data(columns, data)

        return res

    def get_all_users(self):
        return self.get_table_contents("users_")

    def get_user_by_id(self, id: int):
        self.cur.execute(f"SELECT * FROM users_ WHERE id_={id}")

        user = self.cur.fetchone()
        if not user:
            return None

        data = user
        columns = self.get_columns("users_")

        res = self.map_columns_and_data(columns, data)[0]

        return res

    def get_user_by_login(self, login: str):
        self.cur.execute(f"SELECT * FROM users_ WHERE login_='{login}'")

        user = self.cur.fetchone()
        if not user:
            return None

        data = user
        columns = self.get_columns("users_")

        res = self.map_columns_and_data(columns, data)[0]

        return res

    def add_user(self, user_data: dict):
        ph = PasswordHasher()
        try:
            login = user_data['login']
            password = user_data['password']
            display_name = user_data['display_name']

        except Exception as e:
            print(e)
            return "Error on data retrieve: " + str(e)

        hashed_password = ph.hash(password)
        # hashed_password = password

        try:
            self.cur.execute(f"INSERT INTO users_ "
                             f"(display_name_, login_, password_hashed_, creation_date_) "
                             f"VALUES ('{display_name}', '{login}', '{hashed_password}', CURRENT_DATE)")

        except Exception as e:
            print(e)
            return "Error on insert: " + str(e)

        self.conn.commit()

        return None

    def check_if_user_exists(self, user_login) -> bool:
        self.cur.execute(f"SELECT id_ FROM users_ WHERE login_ = '{user_login}'")
        res = self.cur.fetchone()
        if res:
            return True
        else:
            return False

    def get_all_toilets(self):
        toilets = self.get_table_contents("toilets_")

        return toilets

    def get_toilet(self, id: int):
        self.cur.execute(f"SELECT * FROM toilets_ WHERE id_={id}")

        toilet = self.cur.fetchone()
        if not toilet:
            return None

        data = toilet
        columns = self.get_columns("toilets_")

        res = self.map_columns_and_data(columns, data)[0]

        return res

    def add_toilet(self, toilet_data: dict):
        try:
            author_id = toilet_data["author_id_"]
            coordinates = toilet_data["coordinates_"]
            place_name = toilet_data["place_name_"]
            is_public = toilet_data["is_public_"]
            disabled_access = toilet_data["disabled_access_"]
            baby_access = toilet_data["baby_access_"]
            parking_nearby = toilet_data["parking_nearby_"]
            creation_date = toilet_data["creation_date_"]
            opening_time = toilet_data["opening_time_"]
            closing_time = toilet_data["closing_time_"]
            cost = toilet_data["cost_"]

        except Exception as e:
            print(e)
            return "Error on data retrieve: " + str(e)

        try:
            self.cur.execute(
                "INSERT INTO toilets_ (author_id_, coordinates_, place_name_, is_public_, disabled_access_, baby_access_, parking_nearby_, creation_date_ ,opening_time_, closing_time_, cost_) "
                f"VALUES ({author_id}, '{coordinates}', '{place_name}', {is_public}, {disabled_access}, {baby_access}, {parking_nearby}, CURRENT_DATE, '{opening_time}', '{closing_time}', {cost});")

        except Exception as e:
            print(e)
            return "Error on insert: " + str(e)

        self.conn.commit()

        return None

    def check_password(self, login_data):
        ph = PasswordHasher()
        login = login_data["login"]
        password = login_data["password"]

        # hashed_password = password

        self.cur.execute(f"SELECT password_hashed_ FROM users_ WHERE login_ = '{login}'")
        try:
            res = self.cur.fetchone()[0]
        except TypeError:
            return None  # if user wasn't even found

        try:
            ph.verify(res, password)
            user = self.get_user_by_login(login)
            return user

        except (VerifyMismatchError, VerificationError, InvalidHashError) as e:
            print(e)
            return None

    def get_all_reviews(self):
        return self.get_table_contents("toilet_reviews_")

    def get_reviews_by_toilet_id(self, id: int):
        self.cur.execute(f"SELECT * FROM toilet_reviews_ WHERE toilet_id_={id}")

        reviews = self.cur.fetchall()

        if not reviews:
            return None

        data = reviews
        columns = self.get_columns("toilet_reviews_")

        res = self.map_columns_and_data(columns, data)

        return res

    def add_review(self, review_data):
        try:
            toilet_id = review_data["toilet_id_"]
            user_id = review_data["user_id_"]
            rating = review_data["rating_"]
            if "review_text_" not in review_data:
                review_text = None
            else:
                review_text = review_data["review_text_"].replace("'", "").replace('"', "")

        except Exception as e:
            print(e)
            return "Error on data retrieve: " + str(e)

        try:
            if review_text is None:
                self.cur.execute(
                    "INSERT INTO toilet_reviews_ (toilet_id_, user_id_, rating_) "
                    f"VALUES ({toilet_id}, {user_id}, {rating})")
            else:
                self.cur.execute(
                    "INSERT INTO toilet_reviews_ (toilet_id_, user_id_, rating_, review_text_) "
                    f"VALUES ({toilet_id}, {user_id}, {rating}, '{review_text}')")

        except Exception as e:
            print(e)
            return "Error on insert: " + str(e)

        self.conn.commit()

        return None

    def get_all_verifications(self):
        return self.get_table_contents("toilet_verifications_")

    def get_verification(self, id: int):
        self.cur.execute(f"SELECT * FROM toilet_verifications_ WHERE id_={id}")

        verification = self.cur.fetchone()
        if not verification:
            return None

        data = verification
        columns = self.get_columns("toilet_verifications_")

        res = self.map_columns_and_data(columns, data)[0]

        return res

    def add_verification(self, verification_data):
        try:
            toilet_id = verification_data["toilet_id"]
            user_id = verification_data["user_id"]
            vote = verification_data["vote"]

        except Exception as e:
            print(e)
            return "Error on data retrieve: " + str(e)

        try:
            self.cur.execute(f"INSERT INTO toilet_verifications_ (toilet_id_, user_id_, vote_)"
                             f"VALUES ({toilet_id}, {user_id}, {vote})")

        except Exception as e:
            print(e)
            return "Error on insert: " + str(e)

        self.conn.commit()

        return None

    def change_name(self, name_change_data):
        try:
            user_id = name_change_data["user_id"]
            new_name = name_change_data["new_name"]
        except Exception as e:
            print(e)
            return "Error on data retrieve: " + str(e)

        try:
            self.cur.execute(f"UPDATE users_ SET display_name_ = '{new_name}' WHERE id_ = {user_id}")

        except Exception as e:
            print(e)
            return "Error on update"

        self.conn.commit()
        return None


if __name__ == '__main__':
    db = DBManager(host, username, password, database_name)

    # print(db.get_all_users())

    # print(db.get_user(1))

    # print(db.check_if_user_exists("abcde"))
