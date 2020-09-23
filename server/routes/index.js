const { Darknet } = require('darknet');

var express = require('express');
var router = express.Router();

// Init
const darknet = new Darknet({
    weights: 'darknet_files/yolov3-tiny.weights',
    config: 'darknet_files/yolov3-tiny.cfg',
    namefile: 'darknet_files/coco.names'
});

/* GET home page. */
router.get('/', function(req, res) {
  res.render('index', { title: 'Express' });
});

/* TEST darknet */
router.get('/darknet', function(req, res) {
  let test = darknet.detect('dog.jpg');
  res.json({res: test});
});

module.exports = router;
