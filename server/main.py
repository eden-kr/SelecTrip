# -*- coding: utf-8 -*-
"""
Created on Thu May 28 15:18:35 2020

@author: 82109
"""
from flask import Flask, jsonify, request
import warnings
from OpenSSL import SSL
import matplotlib.pyplot as plt
from PIL import Image
import pymysql
import json
import ssl
import selectrip_cnn as sc
import jsonGenerator as gen
import haversign as haver
import crypto as crypt
import sendMail as mail

warnings.simplefilter("ignore", DeprecationWarning)
        
app = Flask(__name__)
db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")

@app.route('/')
def main():
    return 'flask server is Ready'

#이미지 받고 딥러닝 후 나라명 전송
@app.route('/selectrip', methods=['GET', 'POST'])
def toCityName():
    if request.method == 'POST':
        f = request.files['file']   #파일객체 불러오기
        img = Image.open(f)
        plt.xticks([])
        plt.yticks([])
        plt.imshow(img)     #받은 파일 서버에 불러오기
        
        string = sc.ImageToName(f)
    print(string)
    return string #return {"name","스위스"}

#mySQL에서 도시명을 받고 그에 맞는 place를 가져올 Post
@app.route('/place',methods = ['POST'])
def getPlaceInfo():
    if request.method == 'POST':
        city = request.form['city']
        tp = request.form['type']
        print("받은 도시명? "+city+"\n받은 타입명? "+tp)    
        cursor = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip").cursor()     #Connection에서 Cursor생성
        if(tp =='핫플레이스'):
            sql = "SELECT * FROM selectrip.PLACE WHERE CITYNAME LIKE(%s) AND TYPE IN(%s)"
            cursor.execute(sql,(city,tp))
        elif(tp == 'all'):
            sql = "SELECT * FROM PLACE;"
            cursor.execute(sql)
        else:
            sql = "SELECT * FROM selectrip.PLACE WHERE CITYNAME LIKE(%s) AND TYPE IN('레스토랑','술집','카페')"
            cursor.execute(sql,(city))
    
        data = gen.dataToJson(cursor.description,cursor.fetchall())
        cursor.close()
        return data

#아이디 중복체크
@app.route('/check',methods =['POST'])
def CheckDuplication():
    data = request.form['userid']
    cursor = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip").cursor()
    checkSQL = 'SELECT COUNT(*) FROM user WHERE user.id LIKE(%s);'
    cursor.execute(checkSQL,(data))
    num = cursor.fetchall()
    print('들어온데이터' ,data)
    print('db조회 결과값' ,num)
    if(num[0][0]==0) :  #결과값 튜플 반환
        check = "중복아님"
    else:
        check = "중복"
    print("반환값", check)
    return check

#로그인
@app.route('/login',methods=['POST'])
def logIn():
    header = ['check','nickname']
    userid = request.form['userid']   
    pw = request.form['password'] 
    cursor = db.cursor()
    try:
        sql = 'SELECT * FROM user WHERE id LIKE(%s)'
        cursor.execute(sql,userid)
        data = cursor.fetchall()
        saved_password = data[0][1]
        flag = crypt.checkPassword(pw,saved_password)
        
        if(flag == True):
            check = '로그인'
            sql = 'SELECT nickname FROM user WHERE id LIKE(%s);'
            cursor.execute(sql,userid)
            nickname = cursor.fetchone()
            tmp = [check,nickname[0]]
            res = dict(x for x in zip(header,tmp))   #인자 패킹 후 닉네임과 같이 반환
            res = json.dumps(res,ensure_ascii=False)
            return res
        else:
            check = '로그인실패'
            tmp = [check,None]
            res = dict(x for x in dict(zip(header,tmp)))   
            res = json.dumps(res,ensure_ascii=False)
            return res
    except Exception as e:
        print(e)
        

#메인 도시명 받아오기
@app.route('/main',methods=['POST'])
def toMain():
    db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
    cursor = db.cursor()
    try:
        sql = "SELECT * FROM maincity "
        cursor.execute(sql)
        data = gen.dataToJson(cursor.description,cursor.fetchall())
        print(data)
        cursor.close()
    except Exception as e :
        print(e)
    return data
   
    
#회원가입
@app.route('/signup',methods = ['POST'])
def signUp():
    res = ''
    if request.method == 'POST':
        data = request.json  #body로 쌓여있는 데이터 가져오기
        userid = data['id']
        ps = data['password']
        nickname = data['nickname']
        hashed_ps = crypt.genHash(ps)   #암호화
        print('hashed_ps',hashed_ps)
        cursor = db.cursor()
        try:
            sql = "INSERT INTO user(ID,PASSWORD,NICKNAME) VALUES (%s,%s,%s);"
            cursor.execute(sql,(userid,hashed_ps,nickname))
            db.commit()
            cursor.close()
            res = '회원가입 완료'
        except Exception as e :
            print(e)
            db.rollback()
    res = jsonify(res)
    return res
#리뷰작성(app Photo 포함)
@app.route('/writereview',methods = ['POST'])
def writeReview():
    if request.method == 'POST':
        data = request.json
        stName = data["STORENAME"]
        review = data["REVIEW"]
        date = data["date_now"]
        userid = data['userid']
        rating = data['rating']
        username = data['USERNAME']
        image = data['image']
        url = data['storageUrl']
        like = data['like_count']
        print('받은 데이터: ',data)
     
        db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
        cursor = db.cursor()
        try:
            check1 = "SET foreign_key_checks=0;"                 #외래키 오류 해제
            cursor.execute(check1)
            sql = "INSERT INTO userreview(STORENAME,REVIEW,USERNAME,rating,userid,date_now,image,storageUrl,like_count) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s);"
            cursor.execute(sql,(stName,review,username,rating,userid,date,image,url,like))
            db.commit()
            cursor.close()
            res = "리뷰작성"
        except Exception as e :
            print(e)
            db.rollback()
    return res

#평점과 리뷰 수 가져오기
@app.route('/preview',methods=['POST'])
def getPlaceReview():
    db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
    cursor = db.cursor()
    try:
        sql = "SELECT storename, COUNT(*) AS reviewCount, SUM(rating)/COUNT(*) AS rating FROM userreview GROUP BY storename"
        cursor.execute(sql)
        review = gen.dataToJson(cursor.description,cursor.fetchall())
        cursor.close()
    except Exception as e:
        print(e)
    return review
#리뷰만 가져오기
@app.route('/review',methods=['POST'])
def getUserReview():
    storeName = request.form['stName']
    db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
    cursor = db.cursor()
    try:
        sql = "SELECT * FROM userreview WHERE storename LIKE (%s);"
        cursor.execute(sql,storeName)
        review = gen.dataToJson(cursor.description,cursor.fetchall())
        cursor.close()
    except Exception as e:
        print(e)
    return review
   
#도시 추천 알고리즘 (하버사인)
@app.route('/distance',methods=['POST'])
def getHaversine():
    lat = float(request.form['lat'])
    lng = float(request.form['lng'])
    result = haver.getDistance(lat,lng)
    res = []
    for i in result:
        name = 'name'
        dic = dict(name = i)
        res.append(dic)
    Json = json.dumps(res,ensure_ascii=False)
    return Json
#사진 그리드뷰에 이미지 전송
@app.route('/photo',methods=['POST'])
def sendImage():
    placeName = request.form['placeName']
    db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
    cursor = db.cursor()
    p = 'p'
    sql = 'SELECT NAME, image FROM place where name LIKE (%s) UNION SELECT storename AS name, image FROM userreview WHERE storename LIKE (%s) AND image NOT LIKE(%s);'
    try:
        cursor.execute(sql,(placeName,placeName,p))
        imgList = gen.dataToJson(cursor.description,cursor.fetchall())
        cursor.close()
    except Exception as e:
        print(e)
    return imgList

#즐겨찾기
@app.route('/bookmark',methods=['POST'])
def setBookmark():
    userId = request.form['userid']
    click = request.form['click']
    place = request.form['place']
    db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
    cursor = db.cursor()
    try:
        if click == 'true':
            sql ='INSERT INTO bookmark(place,userId) VALUES(%s,%s);'
            cursor.execute(sql,(place,userId))
        else:
            sql ='DELETE FROM bookmark WHERE place LIKE(%s) AND userId LIKE(%s);'
            cursor.execute(sql,(place,userId))
        res = "성공"
        db.commit()
        cursor.close()
    except Exception as e :
        print(e)
        db.rollback()
    return res
#즐겨찾기 받아오기
@app.route('/getbookmark',methods = ['POST'])
def getBookmark():
    userId = request.form['userid']
    place = request.form['place']
    
    print(userId,place)
    db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
    cursor = db.cursor()
    res = '0'
    try:
        sql = 'SELECT COUNT(*) FROM bookmark WHERE userid LIKE(%s) AND place LIKE(%s);'
        cursor.execute(sql,(userId,place))
        flag = cursor.fetchall()
        print(flag[0][0])
        if(flag[0][0]!=0):
            res = '1'
        cursor.close()
    except Exception as e:
        print(e)
    return res


#마이페이지 리뷰, 즐겨찾기 정보 가져오기
@app.route('/mypage',methods = ['POST'])
def getMyPage():
    userid = request.form['userid']
    content_type = request.form['type']
    print("아이디는",userid,"타입은",content_type)
    db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
    cursor = db.cursor()
    res = 0
    try:
        if content_type == 'bookmark':
            sql = 'SELECT p.ID,p.NAME,p.TYPE,p.ADDRESS,p.tel,p.lat,p.lng,p.image,p.cityname,p.hour FROM place p, bookmark b WHERE p.NAME = b.place and b.userId LIKE(%s);'
            cursor.execute(sql,userid)
        else:
            sql = 'SELECT * FROM userreview WHERE userid LIKE(%s);'
            cursor.execute(sql,userid)
        res = gen.dataToJson(cursor.description,cursor.fetchall())
        cursor.close()
        print(res)
    except Exception as e:
        print(e)
    return res

@app.route('/search',methods=['POST'])
def getAllPlace():
    word = request.form['word']
    print('받아온 단어',word)
    db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
    cursor = db.cursor()
    res = ' '
    try:
        wort = '%'+word+'%'
        sql = 'SELECT * FROM place where NAME LIKE(%s) OR TYPE LIKE(%s)'
        cursor.execute(sql,(wort,wort))
        res = gen.dataToJson(cursor.description,cursor.fetchall())
        cursor.close()

        print(res)
    except Exception as e:
        print(e)
    return res

@app.route('/delete', methods=['POST'])
def deleteReview():
    postId = request.form['ID']
    print(postId)
    db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
    cursor = db.cursor()
    res = ''
    try:
        sql = 'DELETE FROM userreview WHERE ID like (%s)'
        cursor.execute(sql,(postId))
        res = "성공"
        db.commit()
        cursor.close()
    except Exception as e:
        db.rollback()
        print(e)
    return res

@app.route('/update',methods=['POST'])
def updateReview():
    postId = request.form['ID']
    newReview = request.form['newReview']
    rate = request.form['rate']
    image = request.form['image']
    url = request.form['url']
    res = ''
    print(newReview,rate,postId,"받아온데이터")
    db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
    cursor = db.cursor()    
    try:
        sql = 'UPDATE userreview SET REVIEW = %s,image = %s, storageUrl = %s, rating = %s WHERE ID like (%s)'
        cursor.execute(sql,(newReview,image,url,rate,postId))
        res = "성공"
        db.commit()
        cursor.close()
    except Exception as e:
        db.rollback()
        print(e)
    return res

@app.route('/like',methods=['POST'])
def setLike():
    userId = request.form['userId']
    postId = request.form['ID']
    clickType = request.form['type']
    count = request.form['count']
    res = ''
    print(clickType)
    db = pymysql.connect(host="localhost", user = "root", password="database", db="selectrip")
    cursor = db.cursor() 
    
    try:
        sql = 'SELECT count(*) FROM likecheck WHERE postId LIKE %s AND userId LIKE(%s);' #사용자가 LIKE를 눌렀는지 체크
        cursor.execute(sql,(postId,userId))
        check = cursor.fetchone()
        if(check[0]!=0):
            sql = 'SELECT flag FROM likecheck WHERE postId LIKE %s AND userId LIKE(%s);'
            cursor.execute(sql,(postId,userId))
            flag = cursor.fetchall()
            if flag[0][0] == 1 and clickType == 'plus':   #클릭한 사람이 다시 클릭하려 하면 fail반환
                res = 'fail'
                print(res)
                return res
            
            elif flag[0][0] == 1 and clickType == 'minus':
                sql ='UPDATE userreview SET like_count = %s WHERE ID LIKE(%s)'  #카운트 감소
                cursor.execute(sql,(count,postId))
                sql = 'UPDATE likecheck SET flag = 0 WHERE postId LIKE(%s) AND userId LIKE(%s)'
                cursor.execute(sql,(postId,userId))
                db.commit()
                res = 'reset'
                print(res)
                return res

            elif flag[0][0] == 0 and clickType == 'plus':
                sql ='UPDATE userreview SET like_count = %s WHERE ID LIKE(%s)'  #카운트 증가
                cursor.execute(sql,(count,postId))
                sql = 'UPDATE likecheck SET flag = 1 WHERE postId LIKE(%s) AND userId LIKE(%s)'
                cursor.execute(sql,(postId,userId))
                db.commit()
                res = 'reok'
                print(res)
                return res
           
        else : #누른적이 있다면
            sql = 'INSERT INTO likecheck(postid, userid, flag) VALUES(%s,%s,1);' #안눌렀으면 새로운 FLAG 생성
            cursor.execute(sql,(postId,userId))
            sql ='UPDATE userreview SET like_count = %s WHERE ID LIKE(%s)'  #카운트 증가
            cursor.execute(sql,(count,postId))
            res = 'ok'
            db.commit()
            print(res)
            return res
    except Exception as e:
        db.rollback()
        print(e)
    cursor.close()
    print(res)
    return res
#비밀번호 찾기 (재설정)
@app.route('/find_password',methods=['POST'])
def findPassword():
    email = request.form['email']
    cursor = db.cursor()
    new_password = mail.createPassword()    #비밀번호 무작위 생성
    hashed_password = crypt.genHash(new_password)   #비밀번호 암호화
    res = ''
    try:
        sql = 'UPDATE user SET PASSWORD = %s WHERE ID LIKE(%s)'
        cursor.execute(sql,(hashed_password,email))
        res = 'success'
        res = jsonify(res)
        mail.sendMail(new_password,email)       #이메일로 비밀번호 전송
        db.commit()
        return res
    except Exception as e:
        db.rollback()
        print(e)
    cursor.close()
#비밀번호 변경
@app.route('/change_password',methods=['POST'])
def changePassword():
    email = request.form['email']
    prev = request.form['prev_pw']
    new =request.form['new_pw']
    cursor = db.cursor()
    try:
        sql = 'SELECT password FROM user WHERE ID LIKE(%s);'
        cursor.execute(sql,email)
        pw = cursor.fetchall()
        if crypt.checkPassword(prev,pw[0][0])==True:    #비밀번호 일치 
            new = crypt.genHash(new)            #Hashed
            sql = 'UPDATE user SET password = %s WHERE ID LIKE(%s);'    #update
            cursor.execute(sql,(new,email))
            db.commit()
            cursor.close()
            res = 'success'
            res = jsonify(res)
            return res
        else :
            res = 'false'
            res = jsonify(res)
            return res
    except Exception as e:
        db.rollback()
        print(e)
    


if __name__ == '__main__':
    ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS)
    ssl_context.load_cert_chain(certfile = 'C:/openssl/bin/cert.pem',keyfile = 'C:/openssl/bin/ca.key',password = '4045859')
    app.run(host='192.168.0.4',ssl_context = ssl_context)
    app.run(threaded=False)
    

    
    
    
    
    
    