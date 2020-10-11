# -*- coding: utf-8 -*-
"""
Created on Sat Aug 22 13:07:54 2020

@author: 82109
"""

import bcrypt

def genHash(password):
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password.encode('UTF-8'),salt)
    #해쉬 비밀번호는 byte -> DB 저장 불가
    #문자열로 디코딩
    return hashed.decode('UTF-8')

def checkPassword(inputPw,savedPw):
    return bcrypt.checkpw(inputPw.encode('UTF-8'),savedPw.encode('UTF-8'))