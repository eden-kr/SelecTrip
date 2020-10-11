# -*- coding: utf-8 -*-
"""
Created on Fri Aug  7 14:20:39 2020

@author: 82109
"""
import json

def dataToJson(header_data,data):
    header = [h[0] for h in header_data]
    res = []
    for d in data :
        res.append(dict(zip(header,d)))
    data = json.dumps(res,ensure_ascii=False)
    print(data)
    return data
    