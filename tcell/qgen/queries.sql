--public static String EP_insertUser =
INSERT INTO User  VALUES (?,?,?,?)
/

--public static String EP_insertFileDesc =
INSERT INTO File  VALUES (?,?,?,?,?,?,?)
/

--public static String EP_deleteFileDescByFileGid =
DELETE FROM File WHERE FILEGID = ?
/


--public static String EP_deleteUserByUserGid =
DELETE FROM User WHERE IdGlobal = ?
/


--public static String EP_isFileGidExists = 
SELECT IdGlobal, FILEGID FROM File WHERE FILEGID = ? 
/

--public static String EP_getUserById = 
SELECT IdGlobal, TCELLIP, PUBLICKEY,TCELLPORT FROM USER WHERE IdGlobal= ?
/

--public static String EP_getFileDescByGid = 
SELECT FILEID, SECRETKEY, IV FROM FILE WHERE FILEGID = ?
/
--public static String EP_getAllUsersGid = 
SELECT IdGlobal from USER
/

--public static String EP_getAllFiles = 
SELECT FILEGID, TYPE, DESCRIPTION FROM FILE
/


--public static String EP_getMyInfo = 
SELECT IdGlobal, MYTCELLIP, MYTCELLPORT, MYPUBLICKEY, MYPRIVATEKEY FROM MyInfo
/


--public static String EP_insertMyInfo =
INSERT INTO MyInfo  VALUES (?,?,?,?,?)
/

--public static String EP_getAllUsers = 
SELECT IdGlobal, TCELLIP, PUBLICKEY,TCELLPORT FROM USER
/

--public static String EP_getAllFilesDesc = 
SELECT IdGlobal ,FILEGID ,FILEID ,SECRETKEY ,IV ,TYPE ,DESCRIPTION FROM FILE
/

