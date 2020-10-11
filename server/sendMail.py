# -*- coding: utf-8 -*-
"""
Created on Sat Aug 22 15:16:32 2020

@author: 82109
"""
import smtplib
from string import digits,punctuation, ascii_letters
import random
from email.mime.text import MIMEText

def createPassword():
    all_string = digits+ascii_letters
    password = ''
    for i in range(9):
        password += random.choice(all_string)
    print(password)
    return password
    
def sendMail(password,user_email):
    try:
        smtp = smtplib.SMTP_SSL('smtp.gmail.com',465)  #SMTP 객체 생성
        smtp.login('yjk7768@gmail.com','bxwnbojhewxpngkk')
        
        msg = MIMEText('새로운 비밀번호를 발급해드립니다 : '+password)
        msg['Subject'] = '[SelecTrip] 비밀번호 변경'
        msg['From'] = 'yjk7768@gmail.com'
        msg['To'] = user_email
        smtp.sendmail('yjk7768@gmail.com',user_email,msg.as_string())
        smtp.quit()
    except Exception as e:
        print(e)
    
    
