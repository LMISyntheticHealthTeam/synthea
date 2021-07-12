import os
try:
    import requests
except ImportError:
    os.system("pip3 install requests")
    import requests
import json
from math import *
import sys


def get_lat_and_lon(address):
    try:
        params_dic = {"key": google_places_key, "address": address}
        result = requests.get(geolocation_base_url, params = params_dic)
        geolocation_json = json.loads(result.text)

        lat = geolocation_json['results'][0]['geometry']['location']['lat']
        lon = geolocation_json['results'][0]['geometry']['location']['lng']
        return (lat, lon)
    except Exception as e:
        raise ValueError("Failed to fetch lat and lon from Google Geolocation API\nAddress: {}".format(address))


def calculate_distance(input1, input2):
    if isinstance(input1, str):
        lat1, lon1 = get_lat_and_lon(input1)
    else:
        lat1, lon1 = input1
    if isinstance(input2, str):
        lat2, lon2 = get_lat_and_lon(input2)
    else:
        lat2, lon2 = input2

    lat1 = radians(lat1)
    lon1 = radians(lon1)
    lat2 = radians(lat2)
    lon2 = radians(lon2)

    def haversin(x):
        return sin(x/2)**2
    return 2 * asin(sqrt(haversin(lat2-lat1) + cos(lat1) * cos(lat2) * haversin(lon2-lon1))) * R


def is_valid_lat_and_lon(lat, lon):
    return -90 <= lat <= 90 and -180 <= lon <= 180


def is_float(string):
    try:
        float(string)
        return True
    except:
        return False


if __name__ == "__main__":
    geolocation_base_url = "https://maps.googleapis.com/maps/api/geocode/json"
    with open("google_api_key.txt") as file:
        google_places_key = file.read().split("=")[1]
    if not google_places_key:
        raise ValueError("Empty Google API key, please write your key to google_api_key.txt")
    R = 6373.0

    args = sys.argv[1:]
    if len(args) == 4:
        try:
            lat1 = float(args[0])
            lon1 = float(args[1])
            lat2 = float(args[2])
            lon2 = float(args[3])

            if not is_valid_lat_and_lon(lat1, lon1) or not is_valid_lat_and_lon(lat2, lon2):
                raise ValueError()
            print("distance:", calculate_distance((lat1, lon1), (lat2, lon2)))
        except Exception as e:
            raise ValueError("Invalid lat and lon values: lat1={}, lon1={}, lat2={}, lon2={}\n{}".format(args[0], args[1], args[2], args[3], str(e)))
    elif len(args) == 2:
        try:
            print("distance:", calculate_distance(args[0], args[1]))
        except Exception as e:
            raise ValueError("Failed to calculate distance given address values:\nAddress1: {}\nAddress2: {}\n{}".format(args[0], args[1], str(e)))
    elif len(args) == 3:
        try:
            if not is_float(args[1]) or (is_float(args[0]) and is_float(args[2])) or (not is_float(args[0]) and not is_float(args[2])):
                raise ValueError()
            if is_float(args[0]):
                lat = float(args[0])
                lon = float(args[1])
                address = args[2]
            else: # is_float(args[2])
                address = args[0]
                lat = float(args[1])
                lon = float(args[2])
            if not is_valid_lat_and_lon(lat, lon):
                raise ValueError()
            print("distance:", calculate_distance((lat, lon), address))
        except Exception as e:
            raise ValueError("Failed to calculate distance given input values: {}, {}, {}".format(args[0], args[1], args[2]))
    else:
        raise ValueError("Invalid parameters: {}".format(",".join(args)))
