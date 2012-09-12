/*jshint sub:true, debug:true, noarg:true, noempty:true, eqeqeq:true, bitwise:true, undef:true, curly:true, browser: true, devel: true, jquery:true */
/*global Backbone, _, jQuery, Camera, FileTransfer, FileUploadOptions, google */

(function(veos) {
  var model = {};

  // model.baseURL = window.location.protocol + "://" + window.location.host + 
  //   (window.location.port ? ':' + window.location.port : '');
  //model.baseURL = "http://backend.veos.ca";
  //model.baseURL = "http://veos.surveillancerights.ca";
  //model.baseURL = "http://192.168.222.108:3000";
  //model.baseURL = "http://192.168.43.221:3000";

  // need full URL for photo uploads to work with reverse proxying
  //model.baseURL = location.protocol + "//" + location.host + "/backend";
  model.baseURL = "http://backend.watch.surveillancerights.ca";

  // model.baseURL = "http://backend.new.surveillancerights.ca"

  jQuery.support.cors = true; // enable cross-domain AJAX requests

  /**
   * Does recursive magic on the given 'attrs' object, renaming
   * each property given in 'nested' array from "myprop" to "myprop_attributes".
   * This is done to make Rails' accepts_nested_attributes_for happy.
   **/
  function wrapNested(nested, attrs) {
    _.each(nested, function (k) {
      if (k instanceof Array) {
        if (attrs[k[0]]) {
          wrapNested(k[1], attrs[k[0]]);
        }
        k = k[0];
      }

      if (attrs[k]) {
        attrs[k+"_attributes"] = attrs[k];
        delete attrs[k];
      }
    });
  }

  var Base = Backbone.Model.extend({
    initialize: function (attributes, options) {
      this.bind("error", this.defaultErrorHandler);
    },
    toJSON: function() {
      var attrs = _.clone( this.attributes ); // WARNING: shallow clone only!
      
      wrapNested(this.nested, attrs);

      var wrap = {};
      wrap[this.singular] = attrs;
      return wrap;
    },
    url: function () {
      var base = model.baseURL + '/' + this.plural;
      if (this.isNew()) {
        return base + '.json';
      }
      else {
        return base + '/' + this.id + '.json';
      }
    },
    defaultErrorHandler: function (model, response, opts) {
      console.error("Error on "+this.singular+" model: " + JSON.stringify(model) + " --- " + JSON.stringify(response));
      
      var msg;

      // FIXME: a 422 response over cross domain will for some reason return status 0... catching it like this here
      //        could result in bogus error reporting.
      if (response.status === 422 || response.status === 0) {
        msg = "Sorry, there is a problem in your "+this.singular+". Please check your input and try again.";
        var errors = {};
        try {
          errors = JSON.parse(response.responseText).errors;
        } catch (err) {
          console.error("Couldn't parse response text: "+response.responseText+ " ("+err+")");
        }

        var errContainer = jQuery("#error-message-container");

        _.each(errors, function(v, k) {
          var errField = jQuery("*[name='"+k+"'].field");

          if (errField.is(':checkbox, :radio')) {
            errField = errField.parent();
          }

          errField.addClass("error");
          jQuery('*[for='+errField.attr('id')+']').addClass("error");
          errField.one('change focus', function() {
            errField.removeClass("error");
            jQuery('*[for='+errField.attr('id')+']').removeClass("error");
          });


          if (errContainer.length !== 0) {
            var humanFieldName = k.replace(/_/, ' ');
            errContainer.append("<li><strong>"+humanFieldName+"</strong> "+v+"</li>");
          }
        });

        if (errContainer.length !== 0) {
          errContainer.show();
        }

      } else if (response.status >= 500 && response.status < 600) {
        msg = "Our apologies, the server responded with an error. There may be a problem with the system.";
      } else {
        msg = "Sorry, there was an error while performing this action. The server may be temporarily unavailable.";
      }

      jQuery('html, body').animate({ scrollTop: 0 }, 0);

      veos.alert(msg, "Error");
    }
  });


  /*** Report ***/

  model.Report = Base.extend({
    singular: "report",
    plural: "reports",
    nested: ['tags', ['photos', ['tags']], ['installation', ['organization']]],
    defaults: {
      'owner_identifiable': true
    },

    // validate: function(attrs) {
    //   console.log("Validating the model...");

    //   var validationObj = {};

    //   // not checking all 'required' fields since it's pretty conditional requirements for now (if B but not A, then...), and there are only 3
    //   // better to check using attrs, if possible, rather than the fields using jQuery

    //   // owner_name or owner_identifiable must be filled
    //   if (!(attrs.owner_name)) {
    //     if (jQuery('#unidentified-owner-checkbox').is(':checked')) {
    //       console.log('passing validation...');          
    //     } else {
    //       alert('Owner name must be filled in or marked as unidentifiable');
    //     }
    //   }

    //   // if owner_name is filled, owner_type must be filled
    //   if (!(attrs.owner_type)) {
    //     if (attrs.owner_name) {
    //       alert('Owner type must be filled out if owner can be identified');
    //     }
    //   }

    //   // _.all(jQuery("input.required").val(), funciton (v) { return v != "" })
    // },  

    attachPhoto: function (photo, successCallback) {
      var report = this;
      photo.save({'report_id': report.id}, {success: function () {
        if (!report.photos) {
          report.photos = [];
        }

        report.photos.push(photo);
        photo.report = report; // in case we need to later refer to the report we're attached to from the photo

        report.updatePhotosAttribute();

        photo.on('change sync', report.updatePhotosAttribute, report);
        
        console.log("Photo "+photo.id+" attached to report "+ report.id);

        if (successCallback) {
          successCallback(report, photo);
        }
      }});
    },

    removePhoto: function (fingerprint) {
      var report = this;

      // this shouldn't really happen...
      if (!report.photos) {
        report.photos = [];
      }

      var photo = _.find(report.photos, function (p) {
        return p.get('image_fingerprint') === fingerprint;
      });

      if (!photo) {
        console.error("Tried to remove a photo with fingerprint '"+fingerprint+"' but this report has no such photo. Attached photos are:",report.photos);
        throw "Tried to remove a photo that doesn't exist in this report!";
      }

      report.photos.splice(_.indexOf(report.photos, photo), 1);
      report.updatePhotosAttribute();
    },

    updatePhotosAttribute: function () {
      if (!this.photos) {
        this.photos = [];
      }

      var photos = [];

      _.each(this.photos, function (photo) {
        photos.push(photo.toJSON()['photo']);
      });

      this.set('photos', photos);
      this.trigger('change');
    },

    addTag: function (tag, tagType) {
      var tags = this.get('tags');
      if (!tags) {
        tags = [];
      }

      tags.push({tag: tag, tag_type: tagType});

      this.set('tags', tags);
      this.trigger('change');
    },

    removeTag: function (tag, tagType) {
      var tags = this.get('tags');

      var t;
      while (this.findTag(tag, tagType)) {
        t = this.findTag(tag, tagType);
        tags.splice(_.indexOf(tags, t), 1);
      }

      this.trigger('change');
    },

    setTags: function (tags, tagType) {
      var ts = _.reject(this.get('tags'), function (t) {
        return t.tag_type === tagType;
      });
      ts = _.uniq(ts, false, function (t) {
        return [t.tag, t.tag_type];
      });
      _.each(_.uniq(tags), function (t) {
        ts.push({tag: t, tag_type: tagType});
      });

      this.set('tags', ts);
    },

    findTag: function (tag, tagType) {
      var tags = this.get('tags');

      return _.find(tags, function (t) {
        return t.tag === tag && t.tag_type === tagType;
      });
    },

    // return attached photos as Photo model objects
    getPhotos: function () {
      return _.map(this.get('photos'), function (data) { return new model.Photo(data);});
    },

    getLatLng: function() {
      if (this.get('loc_lat_from_user')) {
        console.log('In getLatLng() returning loc from user. Lat: '+this.get('loc_lat_from_user')+' Lng: '+this.get('loc_lng_from_user')+'');
        return new google.maps.LatLng(this.get('loc_lat_from_user'), this.get('loc_lng_from_user'));
      } else if (this.get('loc_lat_from_gps')) {
        console.log('In getLatLng() returning loc from GPS. Lat: '+this.get('loc_lat_from_gps')+' Lng: '+this.get('loc_lng_from_gps')+'');
        return new google.maps.LatLng(this.get('loc_lat_from_gps'), this.get('loc_lng_from_gps'));
      } else {
        return null;
      }
    },

    getLocDescription: function() {
      return this.get('loc_description_from_user') || this.get('loc_description_from_google') || "";
    }
  });

  model.Reports = Backbone.Collection.extend({
      model: model.Report,
      url: model.baseURL + '/reports.json'
  });

  /*** Installation ***/

  model.Installation = Base.extend({
    singular: "installation",
    plural: "installations",

    getLocDescription: function() {
      return this.get('loc_description') || "";
    },

    getTruncatedLocDescription: function() {
      var locText = this.get('loc_description') || "";
      return locText.substring(0,24) + '...';
    },

    startAmending: function () {
      var newReport = new model.Report();
      newReport.fetch({url: model.baseURL + '/installations/' + this.id + '/amend.json'});
      return newReport;
    }

  });

  model.Installations = Backbone.Collection.extend({
      model: model.Installation,
      url: model.baseURL + '/installations.json'
  });

  model.NearbyInstallations = Backbone.Collection.extend({
      initialize: function (nearLat, nearLng, maxDist) {
        this.nearLat = nearLat;
        this.nearLng = nearLng;
        this.maxDist = maxDist;
      },
      model: model.Installation,
      url: function () {
        return model.baseURL + '/installations/near.json?lat=' + this.nearLat + '&lng=' + this.nearLng + '&max_dist=' + this.maxDist;
      }
  });

  /*** Organization ***/

  model.Organization = Base.extend({
    singular: "organization",
    plural: "organizations"
  });

  /*** Photo ***/

  model.Photo = Base.extend({
    singular: "photo",
    plural: "photos",
    nested: ['tags'],

    captureFromCamera: function () {
      console.log("Trying to capture photo via camera");
      this.capture(Camera.PictureSourceType.CAMERA);
    },

    captureFromGallery: function () {
      console.log("Trying to select photo from gallery");
      this.capture(Camera.PictureSourceType.PHOTOLIBRARY);
    },

    capture: function (from) {
      var photo = this;
      var options = {
        quality: 50,
        destinationType: Camera.DestinationType.FILE_URI,
        encodingType: Camera.EncodingType.JPEG,
        sourceType: from,
        correctOrientation: true,
        saveToPhotoAlbum: false // don't save photos captured by VEOS to local library
      };

      console.log('Capturing photo from source '+from+' with options: ', options);
      navigator.camera.getPicture(
        function (imageURL) { 
          photo.imageURL = imageURL;
          photo.trigger('image_capture', imageURL);
        }, 
        function (error) { 
          console.error("Image capture failed: " + JSON.stringify(error));
          photo.trigger('image_capture_error', error);
        }, 
        options
      );
    },

    upload: function () {
      var photo = this;

      if (!photo.imageURL) {
        throw new Error("Cannot upload photo because it does not have an imageURL! You need to capture an image before uploading.");
      }

      console.log("Uploading photo: "+photo.imageURL);
      photo.trigger('image_upload_start');

      var options = new FileUploadOptions();
      options.fileKey = "photo[image]";
      options.fileName = photo.imageURL.substr(photo.imageURL.lastIndexOf('/')+1);
      options.mimeType = "image/jpeg";
      // chunkedMode false uses more memory and might lead to crashes after some pictures
      // chunkedMode true can lead to a situation where no data is transmitted to the server
      // this seems to be fixed by setting the 6th value in transfer.upload to true (acceptSelfSignedCert)
      // while I am not sure how that affects a unencrypted http connection it seems to work
      // Sep 11, 2012 doesn't seem to work so set to false
      options.chunkedMode = false;

      var success = function (res) {
        console.log("Image uploaded successfully; "+res.bytesSent+" bytes sent.");
        
        res.photo = JSON.parse(res.response);
        photo.set('id', res.photo.id);

        console.log("Assigned id to photo: "+photo.id);

        photo.set(res.photo);

        photo.trigger('image_upload_finish', res);
      };

      var failure = function (error) {
        console.error("Image upload failed: " + JSON.stringify(error));
        photo.trigger('image_upload_error', error);
      };

      var transfer = new FileTransfer();
      /**
        filePath, server, successCallback, failureCallback, options, acceptSelfSignedCert
      **/
      transfer.upload(photo.imageURL, photo.url(), success, failure, options, true);
    },

    addTag: function (tag) {
      var tags = this.get('tags');
      if (!tags) {
        tags = [];
      }

      tags.push({tag: tag});

      this.set('tags', tags);
      this.trigger('change');
    },

    removeTag: function (tag) {
      var tags = this.get('tags');

      var t;
      while (this.findTag(tag)) {
        t = this.findTag(tag);
        if (t.id) {
          t._destroy = true;
        } else {
          tags.splice(_.indexOf(tags, t), 1);
        }
      }

      this.trigger('change');
    },

    setTags: function (tags) {
      var ts = [];
      this.set('tags', ts);
      _.each(tags, function (t) {
        ts.push({tag: t});
      });
    },

    findTag: function (tag) {
      var tags = this.get('tags');

      return _.find(tags, function (t) {
        return t.tag === tag && !t._destroy;
      });
    },

    thumbUrl: function () {
      return model.baseURL + "/" + this.get('thumb_url');
    },

    bigUrl: function () {
      return model.baseURL + "/" + this.get('big_url');
    },

    originalUrl: function () {
      return model.baseURL + "/" + this.get('original_url');
    }
  });


  veos.model = model;
})(window.veos);
