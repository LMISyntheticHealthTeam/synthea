import os
try:
    import requests
except ImportError:
    os.system("pip3 install requests")
    import requests
import json
import sys


def calculate_distance_and_travel_time(input1, input2, travel_mode="driving"):
    if not isinstance(input1, str):
        input1 = "{},{}".format(input1[0], input1[1])
    if not isinstance(input2, str):
        input2 = "{},{}".format(input2[0], input2[1])

    try:
        params_dic = {"key": google_places_key,
                      "origins": input1,
                      "destinations": input2,
                      "mode": travel_mode
                     }
        result = requests.get(google_distance_matrix_base_url, params = params_dic)
        distance_matrix_json = json.loads(result.text)
        # print(distance_matrix_json) # uncomment this line if you want to see what Google returns

        distance = distance_matrix_json['rows'][0]['elements'][0]['distance']['value'] / 1000 * 0.621371
        travel_time = distance_matrix_json['rows'][0]['elements'][0]['duration']['value'] / 60

        return (distance, travel_time)

    except Exception as e:
        raise ValueError("Failed to fetch distance and travel time from Google API\nPlace 1: {}, Place 2: {}".format(input1, input2))


def is_valid_lat_and_lon(lat, lon):
    return -90 <= lat <= 90 and -180 <= lon <= 180


def is_float(string):
    try:
        float(string)
        return True
    except:
        return False

## The codes takes two, three, or four parameters. They can be two addresses, one address + lat & lon, and
## two lat & lon pairs.
    ## Example 1:
    ## python3 distance_and_travel_time_calculation.py "7940 Jones Branch Dr, Tysons, VA 22102" "3101 Wisconsin Ave NW, Washington, DC 20016"
    ## Example 2:
    ## python3 distance_and_travel_time_calculation.py "7940 Jones Branch Dr, Tysons, VA 22102" 38.930206 -77.0732444
    ## Example 3:
    ## python3 distance_and_travel_time_calculation.py 38.930206 -77.0732444 "7940 Jones Branch Dr, Tysons, VA 22102"
    ## Example 4:
    ## python3 distance_and_travel_time_calculation.py 38.930206 -77.0732444 38.930379 -77.2162014
## Example output (distance in miles, travel time in minutes)
## distance: 9.733776715   travel time: 24.433333333333334

if __name__ == "__main__":
    google_distance_matrix_base_url = 'https://maps.googleapis.com/maps/api/distancematrix/json?'
    with open("google_api_key.txt") as file:
        google_places_key = file.read().split("=")[1]
    if not google_places_key:
        raise ValueError("Empty Google API key, please write your key to google_api_key.txt")

    args = sys.argv[1:]
    if len(args) == 4:
        try:
            lat1 = float(args[0])
            lon1 = float(args[1])
            lat2 = float(args[2])
            lon2 = float(args[3])

            if not is_valid_lat_and_lon(lat1, lon1) or not is_valid_lat_and_lon(lat2, lon2):
                raise ValueError()
            distance, travel_time = calculate_distance_and_travel_time((lat1, lon1), (lat2, lon2))
            print("distance:", distance, "  travel time:", travel_time)
        except Exception as e:
            raise ValueError("Invalid lat and lon values: lat1={}, lon1={}, lat2={}, lon2={}\n{}".format(args[0], args[1], args[2], args[3], str(e)))
    elif len(args) == 2:
        try:
            distance, travel_time = calculate_distance_and_travel_time(args[0], args[1])
            print("distance:", distance, "  travel time:", travel_time)
        except Exception as e:
            raise ValueError("Failed to calculate distance and travel time given address values:\nAddress1: {}\nAddress2: {}\n{}".format(args[0], args[1], str(e)))
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
            distance, travel_time = calculate_distance_and_travel_time((lat, lon), address)
            print("distance:", distance, "  travel time:", travel_time)
        except Exception as e:
            raise ValueError("Failed to calculate distance and travel time given input values: {}, {}, {}".format(args[0], args[1], args[2]))
    else:
        raise ValueError("Invalid parameters: {}".format(",".join(args)))
