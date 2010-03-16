var baseUrl = "http://umjammer03.appspot.com/lr";
var mixi = null;
var displayName = null;
var thumbnailUrl = null;

function setScoreCallback() {
  var src = baseUrl + "/highscore/" + mixi;
  document.getElementById('highscore').src = src;
}

function setScore(score) {
  var params = {};
  params[gadgets.io.RequestParameters.AUTHORIZATION] = gadgets.io.AuthorizationType.SIGNED;
  gadgets.io.makeRequest(baseUrl + "/score/" + mixi + "/" + score, setScoreCallback, params);
}

function init() {
  var req = opensocial.newDataRequest();
  req.add(req.newFetchPersonRequest(opensocial.IdSpec.PersonId.VIEWER), "viewer");
  req.send(function(data) {
    var viewer = data.get("viewer").getData();
    //
    mixi = viewer.getId();
    displayName = viewer.getDisplayName();
    thumbnailUrl = viewer.getField(opensocial.Person.Field.THUMBNAIL_URL);
//  var profileUrl = viewer.getField(opensocial.Person.Field.PROFILE_URL);
//  var address = viewer.getField(opensocial.Person.Field.ADDRESSES)[0].getField(opensocial.Address.Field.UNSTRUCTURED);
//  var name = viewer.getField(opensocial.Person.Field.NAME).getField(opensocial.Name.Field.UNSTRUCTURED);
//  var nickName = viewer.getField(opensocial.Person.Field.NICKNAME);
//  var gender = viewer.getField(opensocial.Person.Field.GENDER).getDisplayValue();
//  var boodType = viewer.getField(mixi.PersonField.BloodType);

    var src = baseUrl + "/highscore/" + mixi;
    var iframedoc;
    if (document.all) {
      iframedoc = document.getElementById("iframe2").contentWindow.document;
    } else {
      iframedoc = document.getElementById("iframe2").contentDocument;
    }
    iframedoc.body.innerHTML = 
     ' Change OpenFeint Avatar <br/>' +
     ' <form action="http://umjammer03.appspot.com/up" method="post" enctype="multipart/form-data">' +
     '  <input type="hidden" name="mixi" value="' + mixi + '"/>' +
     '  <input type="file" name="file" />' +
     '  <input type="submit" value="upload" />' +
     ' </form>' +
     '<br/>' +
     '<img src="' + thumbnailUrl + '"/>';
    if (document.all) {
      iframedoc = document.getElementById("iframe3").contentWindow.document;
    } else {
      iframedoc = document.getElementById("iframe3").contentDocument;
    }
    iframedoc.body.innerHTML = 
     ' Change OpenFeint Name <br/>' +
     ' <form action="http://umjammer03.appspot.com/lr/rename" method="post" enctype="application/x-www-form-urlencoded">' +
     '  <input type="hidden" name="mixi" value="' + mixi + '"/>' +
     '  <input type="text" name="name" value="' + displayName + '" />' +
     '  <input type="submit" value="rename" />' +
     ' </form>';
    document.getElementById('highscore').src = src;
  });
}

gadgets.util.registerOnLoadHandler(init);
