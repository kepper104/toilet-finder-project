import unittest, requests


class MyTestCase(unittest.TestCase):
    def test_something(self):
        self.assertEqual(True, True)  # add assertion here


if __name__ == '__main__':
    unittest.main()


# TODO Write tests

# class RestCalls():
#
#     def google_do_something(blahblah):
#         url= blahblah
#         try:
#             r = requests.get(url,timeout=1)
#             r.raise_for_status()
#             return r.status_code
#         except requests.exceptions.Timeout as errt:
#             print (errt)
#             raise
#         except requests.exceptions.HTTPError as errh:
#             print (errh)
#             raise
#         except requests.exceptions.ConnectionError as errc:
#             print (errc)
#             raise
#         except requests.exceptions.RequestException as err:
#             print (err)
#             raise
#
#
# class TestRESTMethods(unittest.TestCase):
#
#     def test_valid_url(self):
#         self.assertEqual(200,RestCalls.google_do_something('http://www.google.com/search'))
#
#     def test_exception(self):
#         self.assertRaises(requests.exceptions.Timeout,RestCalls.google_do_something,'http://localhost:28989')
