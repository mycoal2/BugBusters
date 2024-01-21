from joblib import dump, load
import json
import os, sys
import firebase_admin
from firebase_admin import firestore
from firebase_admin import db
from firebase_admin import credentials
import tqdm
import time
import traceback


#print current working directory
print(os.getcwd())


cred = credentials.Certificate(r'BugBusters\data_training\bugbusters-c6a2c-firebase-adminsdk-5sm2b-50a8ebf152.json')
# firebase_admin.initialize_app(cred)





#load the model
print("Loading models...")
model_incoming_bikes = load(r'BugBusters\data_training\random_forest_model_incoming.joblib')
model_outgoing_bikes = load(r'BugBusters\data_training\random_forest_model_outgoing.joblib')
print("Models loaded!")



#load json files
print("Loading json files...")
with open('BugBusters\data_training\id_to_station.json', 'r') as fp:
    id_to_station = json.load(fp)

with open('BugBusters\data_training\station_to_id.json', 'r') as fp:
    station_to_id = json.load(fp)

with open ('BugBusters\data_training\station_coordinates.json', 'r') as fp:
    station_to_lat = json.load(fp)
fp.close()
print("Json files loaded!")

#add to a list

default_app = firebase_admin.initialize_app(cred, {'databaseURL': 'https://bugbusters-c6a2c-default-rtdb.firebaseio.com/'})
firestore_db = firestore.client()

while (True):
    try:
        ref = db.reference('request')
        info_in = ref.get()
        if (info_in != None):
            info_in_dict = info_in
            print(info_in_dict)
            if (info_in_dict['request_in']):
                print("Request in!")
                lat_target = info_in_dict['information']['lat']
                lon_target = info_in_dict['information']['lon']
                hour_target = info_in_dict['information']['hour']
                day_target = info_in_dict['information']['day_of_week']
                month_target = info_in_dict['information']['month']
                is_incoming_bike = info_in_dict['information']['is_incoming_bike']
                id = None
                for key in (station_to_lat.keys()):
                    if (station_to_lat[key]['lat'] == lat_target and station_to_lat[key]['long'] == lon_target):
                        id = station_to_id[key]
                        print("Found id!")
                        print(id)
                        break
                if (id != None):
                    category = None
                    if (is_incoming_bike):
                        prediction = model_incoming_bikes.predict([[id, month_target, hour_target, day_target]])[0]
                        if (prediction < 0.002458):
                            category = "Unlikely"
                        elif (prediction >= 0.002458 and prediction< 0.011059):
                            category = "Somewhat unlikely"
                        elif (prediction >= 0.011059 and prediction <  0.023282):
                            category = "Likely"
                        else:
                            category = "Very likely"
                    else:
                        prediction = model_incoming_bikes.predict([[id, month_target, hour_target, day_target]])[0]
        
                        if (prediction < 0.003558):
                            category = "Unlikely"
                        elif (prediction >= 0.003558 and prediction< 0.011859):
                            category = "Somewhat unlikely"
                        elif (prediction >= 0.011859 and prediction <  0.023717):
                            category = "Likely"
                        else:
                            category = "Very likely"
                    print("Prediction: ", prediction)
                    ref = db.reference('request')
                    ref.update({
                        'request_in': False,
                        'information_out': {
                            'cat': category,
                            'norm_value' : prediction
                        },
                        'request_complete': True
                    })
                    print("Sent!")
                    
    except:
        print("Something Came up")
        print(traceback.format_exc())

        
    time.sleep(0.5)


# for key in (station_to_id.keys()):
#     print ("Progress: ", i, "/", len(station_to_id.keys()))
#     for month in range(0,12):
#         for day_of_week in range(0,7):
#             for hour in range(0,24):
#                 model_incoming_bikes_arr.append ({"station_id": key, "station_name": station_to_id[key],"month": month, "day_of_week": day_of_week, "hour": hour, "prediction": model_incoming_bikes.predict([[key, month, hour, day_of_week]])})

# print(model_incoming_bikes)
