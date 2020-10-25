const {
    Darknet
} = require('darknet');
const multer = require('multer');

var express = require('express');
var router = express.Router();
var fs = require('fs');

// Init
const darknet = new Darknet({
    weights: 'darknet_files/yolov3-tiny.weights',
    config: 'darknet_files/yolov3-tiny.cfg',
    namefile: 'darknet_files/coco.names'
});

var _storage = multer.diskStorage({
    // upload 폴더에 저장
    destination: function (req, file, cb) {
        cb(null, 'image/')
    },
    // 저장할 파일 이름 정하기
    filename: function (req, file, cb) {
        // orignalname.jpg 형식으로 저장
        cb(null, file.originalname)
    }
});

var upload = multer({
    storage: _storage
});


/* GET home page. */
router.get('/', function (req, res) {
    res.render('index', {
        title: 'Express'
    });
});

/* TEST darknet */
router.post('/upload', upload.single('pngfile'), function (req, res) {
    let file = req.file;
    let dark = darknet.detect('image/' + file.originalname);
    console.log(dark);
    res.json(dark);
});

/* delete all */
router.delete('/delete', function (req, res) {
    function deleteFiles() {
        return new Promise((resolve, reject) => {
            var path = __dirname + "/../image/";
            if (fs.existsSync(path)) {
                // readdirSync(path): 디렉토리 안의 파일의 이름을 배열로 반환
                fs.readdirSync(path).forEach(function (file, index) {
                    var curPath = path + "/" + file;
                    if (fs.lstatSync(curPath).isDirectory()) { // lstatSync: stat값을 반환함, isDirectory(): 디렉토리인지 파악
                        deleteFolderRecursive(curPath); // 재귀(reCurse)
                    } else { // delete file
                        fs.unlinkSync(curPath); // unlinkSync: 파일 삭제
                    }
                })
                resolve("success");
                //fs.rmdirSync(path); // rmdirSync: 폴더 삭제
            }else{
                reject("no dir");
            }
        })
    }
    deleteFiles()
        .then( r => {
            res.json({
                "res": r
            });
        })
        .catch( err =>{
            console.log(err);
            res.json({
                "res": err
            });
        });
})

module.exports = router;
