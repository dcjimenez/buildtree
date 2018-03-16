// Build and populate the jstree
jQuery(document).ready(function () { 	
	// Make sure that no other plugin that is using jQuery will cause us any problem
	//	(namely extended-choice-parameter, for example)	    	
	var url = jQuery("#rootUrlStore").val()
	jQuery.getScript(url + '/plugin/buildtree/scripts/jstree.min.js', function () {	
		jQuery('#build_tree').html(jQuery('#temporalStore').val());
		jQuery('#build_tree').on("changed.jstree", function (e, data) {
		      //console.log(data.instance.get_node(data.selected[0]).text);
		      // Open the link to the log
		      var link = data.instance.get_node(data.selected[0]).state.logURL;
		      var win = window.open(link, '_blank');
			  if (win) {
			    // Browser has allowed it to be opened
			    win.focus();
			  } 
		    }).jstree({
		      'core': {
				'multiple': false
			  },
		      "plugins" : [ "changed" ]
		    }); 
		jQuery('#build_tree').jstree().open_all();
		jQuery.jstree.defaults.core.multiple = false;
	});
});