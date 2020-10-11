# -*- coding: utf-8 -*-
"""
Created on Fri Aug  7 16:46:49 2020

@author: 82109
"""
import math
import pymysql

def hsin(myLat,myLng,lat,lng):  #하버사인 공식
    radius = 6371       
    radian = math.pi/180
    myLat *= radian
    myLng *= radian
    lat *= radian
    lng *= radian
    result =  2*radius*math.asin(math.sqrt(pow(math.sin(abs(lat-myLat))/2,2)+
                                           math.cos(myLat)*math.cos(lat)*
                                           pow(math.sin(abs((lng-myLng)/2)),2)))

    return result

#하버사인 공식을 적용해서 가장 가까운 top6도시 리스트 반환
def getDistance(lat,lng):
    db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
    cursor = db.cursor()
    sql = "select city,lat,lon from maincity"
    cursor.execute(sql)
    data= cursor.fetchall()
    res = []        #결과값 반환할 리스트
    
    name = [i[0] for i in data]     #나라명
    latlng = [(i[1],i[2]) for i in data]    #위도경도
    country = dict(zip(name,latlng))        #나라명 : 위도경도 dictionary
    for i in name:
        res.append(hsin(country.get(i)[0],country.get(i)[1],lat,lng))       #하버사인 공식 적용
    result = dict(i for i in zip(name,res))        #딕셔너리 형 반환
    sorted_result = sorted(result, key=result.__getitem__)     #정렬된 결과값
    top5 = sorted_result[1:6]       #top5까지의 도시명 구하기
    cursor.close()

    return top5





