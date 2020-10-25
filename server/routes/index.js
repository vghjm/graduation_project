const { Darknet } = require('darknet');
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

var upload = multer({ storage : _storage });


/* GET home page. */
router.get('/', function(req, res) {
  res.render('index', { title: 'Express' });
});

/* TEST darknet */
router.post('/upload', upload.single('pngfile'), function(req, res) {
 
  let file = req.file;  
  
  let dark = darknet.detect('image/'+file.originalname);
  console.log(dark);
   res.json(dark);

});

module.exports = router;
