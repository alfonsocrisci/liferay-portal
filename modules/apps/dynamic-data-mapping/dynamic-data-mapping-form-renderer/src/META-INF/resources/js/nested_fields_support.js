AUI.add(
	'liferay-ddm-form-renderer-nested-fields',
	function(A) {
		var AArray = A.Array;

		var NestedFieldsSupport = function() {
		};

		NestedFieldsSupport.ATTRS = {
			fields: {
				validator: Array.isArray,
				value: []
			}
		};

		NestedFieldsSupport.prototype = {
			initializer: function() {
				var instance = this;

				instance._eventHandlers.push(
					instance.after('containerChange', instance._afterContainerChange),
					instance.after('fieldsChange', instance._afterFieldsChange)
				);

				instance._updateFieldsParent(instance.get('fields'));
			},

			destructor: function() {
				var instance = this;

				AArray.invoke(instance.get('fields'), 'destroy');
			},

			appendChild: function(field) {
				var instance = this;

				instance.insert(0, field);
			},

			eachField: function(fn) {
				var instance = this;

				var queue = new A.Queue();

				var addToQueue = function(item) {
					queue.add(item);
				};

				instance.get('fields').forEach(addToQueue);

				while (queue.size() > 0) {
					var field = queue.next();

					var stop = fn.call(instance, field, queue);

					if (stop === true) {
						break;
					}

					field.get('fields').forEach(addToQueue);
				}
			},

			eachParent: function(fn) {
				var instance = this;

				var parent = instance.get('parent');

				while (parent !== undefined) {
					fn.call(instance, parent);

					parent = parent.get('parent');
				}
			},

			filterNodes: function(fn) {
				var instance = this;

				var nodes = instance.get('container').all('.lfr-ddm-form-field-container');

				return nodes.filter(
					function(item) {
						var qualifiedName = item.one('.form-group').getData('fieldname');

						return fn.call(instance, qualifiedName, item);
					}
				);
			},

			getField: function(name) {
				var instance = this;

				var field;

				instance.eachField(
					function(item) {
						if (item.get('name') === name) {
							field = item;
						}

						return field !== undefined;
					}
				);

				return field;
			},

			getRoot: function() {
				var instance = this;

				var root;

				instance.eachParent(
					function(parent) {
						root = parent;
					}
				);

				return root;
			},

			indexOf: function(field) {
				var instance = this;

				return instance.get('fields').indexOf(field);
			},

			insert: function(index, field) {
				var instance = this;

				instance.removeChild(field);

				var fields = instance.get('fields');

				fields.splice(index, 0, field);

				instance.set('fields', fields);
			},

			removeChild: function(field) {
				var instance = this;

				var fields = instance.get('fields');

				var index = instance.indexOf(field);

				if (index > -1) {
					fields.splice(index, 1);
				}

				instance.set('fields', fields);
			},

			_afterContainerChange: function(event) {
				var instance = this;

				A.each(
					instance.get('fields'),
					function(item) {
						event.newVal.append(item.get('container'));
					}
				);
			},

			_afterFieldsChange: function(event) {
				var instance = this;

				instance._updateFieldsParent(event.newVal);
			},

			_updateFieldsParent: function(fields) {
				var instance = this;

				fields.forEach(
					function(field) {
						if (field.get('parent') !== instance) {
							field.set('parent', instance);
						}
					}
				);
			}
		};

		Liferay.namespace('DDM.Renderer').NestedFieldsSupport = NestedFieldsSupport;
	},
	'',
	{
		requires: ['liferay-ddm-form-renderer-types', 'liferay-ddm-form-renderer-util']
	}
);