$(document).ready(function () {
    
    // Reset the query entry
    $("#btnResetQuery").click(function (event) {
        location.reload();
    });
    
    // submitting the query to run the operation
    $("#btnSubmitQuery").click(function (event) {
        
        //stop submit the form, we will post it manually.
        event.preventDefault();
        
        if (!isValidForm()) return;
        
        let data = createRequestData();
        
        $.ajax({
            type: "GET",
            url: "/bin/quakshop/queryTool",
            data: data,
            processData: false,
            contentType: false,
            cache: false,
            success: function (data) {
                
                $("#btnSubmitQuery").attr("disabled", true);
                $('.coral-Form-field, [coral-multifield-add]').each(function (index) { $(this).attr('disabled', true) });
                
                removeErrorMessages();
                processQueryResults(data);
                
            },
            error: function (e) {
                console.error(e);
            }
        });
        
        let processQueryResults = function (data) {
            
            const $result = $('.result-table');
            
            let resultOutput, operation,
                tableData = (JSON.parse(data) || data),
                hasResults = (tableData && tableData.result && Object.keys(tableData.result).length);
            
            if (hasResults) {
                
                resultOutput = buildResultTable(tableData);
                operation = (tableData.operation || "read");
                
                if (operation !== "read") {
                    let modifyDialog = modifyConfimDialog(operation),
                        modifyContainer = document.createElement('div');
                    
                    modifyContainer.append(modifyDialog.alert);
                    modifyContainer.append(modifyDialog.dialog);
                    $result.append(modifyContainer);
                    
                    // add click function to row selection
                    $result.on('click', '[is="coral-table-row"][selected]', function (event) {
                        
                        const target = $(event.target);
                        const dialog = $('#methodActionDialog')[0];
                        const nodePath = (target.find(".result-node--path")[0] || target.parent().find(".result-node--path")[0]);
                        const nodeData = "path=" + nodePath.innerText;
                        
                        debugger;
                        
                        $.ajax({
                            type: "GET",
                            url: "/bin/quakshop/queryTool/nodeProperties",
                            data: nodeData,
                            processData: false,
                            contentType: false,
                            cache: false,
                            success: function (propertyData) {
                                debugger;
                                const resultData = JSON.parse(propertyData) || propertyData;
                                console.log(resultData);
                                
                                propertiesDialog(resultData).show().on('click', '#nextButton', function () {
                                    console.log("NEXT!");
                                    $(this).hide();
                                    dialog.show().on('click', '#proceedButton', function () {
                                        console.log("PROCEED!");
                                        $(this).hide();                                        
                                    });                                    
                                });
                                
                                /*
                                dialog.on('click', '#proceedButton', function () {
                                console.log("proceed!");
                                $(this).hide();
                                }).show();
                                */
                                
                                
                                dialog.find(".coral3-Dialog-content").append();
                                
                            },
                            error: function (e) {
                                alert("Error in Display Node Properties!");
                                console.error(e);
                            }
                        });
                        
                        
                    });
                }
                
                $("#success").attr('hidden', false);
                
            } else if (tableData && tableData.query) {
                //resultOutput = emptyResultsMessge;
                $("#noResults").append(tableData.query);
                $("#noResults").attr('hidden', false);
                
            }
            
            $result.append(resultOutput);
            $result.addClass('result-extra-padding');
            
        }
        
        let buildResultTable = function (tableData) {
            
            
            let resultData = JSON.parse(tableData.result),
                queryText = tableData.query,
                finalTable = '<table is="coral-table" selectable id="result-table-element" class="coral--light"><colgroup>',
                number_of_cols = 5,
                number_of_rows = Object.keys(resultData).length,
                count_i = 1;
            
            $('#success').append('<br><br><div>' + number_of_rows + ' Results from Query:<br><br>' + queryText + '</div>');
            
            for (var i = 0; i < number_of_cols; i++) {
                if (i < 2)
                    finalTable += '<col is="coral-table-column" sortable sortabletype="number">';
                else
                    finalTable += '<col is="coral-table-column" sortable>';
            }
            
            finalTable += '</colgroup><thead is="coral-table-head" sticky><tr is="coral-table-row">'
            + '<th is="coral-table-headercell">Item #</th>'
            + '<th is="coral-table-headercell">Path</th>'
            + '<th is="coral-table-headercell">Name</th>'
            + '<th is="coral-table-headercell">Type</th>'
            + '<th is="coral-table-headercell">Properties</th>'
            + '</tr></thead>'
            + '<tbody is="coral-table-body">';
            
            for (let i in resultData) {
                
                let count_j = 0,
                    entry = JSON.parse(resultData[i]),
                    fields = [];
                
                for (let key in entry) {
                    fields.push(entry[key]);
                }
                
                finalTable += '<tr is="coral-table-row">';
                
                for (var j = 0; j < number_of_cols; j++) {
                    
                    
                    if (j === 0) {
                        finalTable += '<td is="coral-table-cell" value="' + count_i + '">';
                        finalTable += count_i++;
                        finalTable += '</td>';
                    } else {
                        if (j === 1) {
                            finalTable += '<td is="coral-table-cell" class="result-node--path">';
                        } else {
                            finalTable += '<td is="coral-table-cell">';
                        }
                        finalTable += fields[count_j++];
                        finalTable += '</td>';
                    }
                }
                finalTable += '</tr>';
            }
            
            finalTable += '</tbody></table>';
            return finalTable;
            
        };
    });
    
    let propertiesDialog = function (data) {
        
        let contentHTML = '';
        
        for(let i in data){
            contentHTML += '<label>'+i+' :</label><br>'+'<input class="coral-Form-field coral3-Textfield" type="text" value="'+ data[i] +'"><br>'
        }
        
        const dialog = new Coral.Dialog().set({
            id: "propertiesListingDialog",
            header: {
                innerHTML: 'Selected Node\'s Properties'
            },
            content: {
                innerHTML: contentHTML
            },
            footer: {
                innerHTML: '<button id="nextButton" is="coral-button" variant="primary" coral-close="" class="coral-Button coral-Button--primary"'
                + ' size="M"><coral-button-label>Next</coral-button-label></button>'
                + '<button is="coral-button" variant="quiet" coral-close="" class="coral-Button coral-Button--primary"'
                + ' size="M"><coral-button-label>Cancel</coral-button-label></button>'
            }
        });
        
        return dialog;
        
    }
    
    let modifyConfimDialog = function (methodType) {
        
        let method = methodType.toUpperCase();
        
        var dialog = new Coral.Dialog().set({
            id: "methodActionDialog",
            header: {
                innerHTML: 'Warning: Modification Action'
            },
            content: {
                innerHTML: '<div>Are you sure you want to continue?<br>'
                + 'Improper changes to the jcr could have some negative side effects.<br>'
                + 'Click "Proceed" to continue with the <b>' + method + '</b> action...</div>'
            },
            footer: {
                innerHTML: '<button id="proceedButton" is="coral-button" variant="primary" coral-close="" class="coral-Button coral-Button--primary"'
                + ' size="M"><coral-button-label>Proceed</coral-button-label></button>'
                + '<button is="coral-button" variant="quiet" coral-close="" class="coral-Button coral-Button--primary"'
                + ' size="M"><coral-button-label>Cancel</coral-button-label></button>'
            }
        });
        
        let alert = new Coral.Alert().set({
            variant: 'warning',
            header: {
                innerHTML: 'INFO'
            },
            content: {
                innerHTML: 'Select an item in table to ' + method + ' data and modify!'
            }
        });
        
        return {
            dialog: dialog,
            alert: alert
        }
        
    }
    
    let removeErrorMessages = function () {
        $("#error").attr('hidden', true);
        $('.invalid-form-field').removeClass("invalid-form-field");
        
    }
    
    let resetDisplay = function () {
        
        $("#btnSubmitQuery").attr("disabled", false);
        $("#operation").attr("disabled", false);
        $(".result-table").empty();
        $('.result-table').removeClass('result-extra-padding');
        
    }
    
    // validate for empty required fields 
    let isValidForm = function () {
        
        let requiredFields = $('[required]');
        
        for (let field of requiredFields) {
            let x = field.value;
            if (!x) {
                let $field = $(field),
                    parentContainer = $field.parents(".methodType-showhide-target");
                
                if (parentContainer && parentContainer.hasClass("hide")) {
                    continue;
                }
                
                $field.addClass("invalid-form-field");
                $("#error").attr('hidden', false);
                
                return false;
            }
        }
        
        return true;
    }
    
    let createRequestData = function () {
        
        let result = '', method = $('#method')[0], operation = $('#operation')[0];
        
        if (method.value === 'manual') {
            
            let querySearch = $('#querySearch')[0],
                queryLanguage = $('#queryLanguage')[0];
            
            if (queryLanguage.value && querySearch.value) {
                result += 'queryLanguage=' + queryLanguage.value;
                result += '&querySearch=' + querySearch.value;
            }
            
        } else {
            
            // Get form
            let searchIn = $('#searchIn')[0],
                fullTextSearch = $('#fullTextSearch')[0],
                primaryType = $('#primaryType')[0],
                searchPropsObject = {}, temp = [];
            
            if (searchIn.value) result += 'searchIn=' + searchIn.value + '&';
            if (fullTextSearch.value) result += 'fullTextSearch=' + fullTextSearch.value + '&';
            if (operation.value) result += 'operation=' + operation.value + '&';
            if (primaryType.value) result += 'primaryType=' + primaryType.value + '&';
            
            $('#searchProps coral-multifield-item').each(function () {
                
                let name = $(this).find("[name$='./name']")[0];
                let value = $(this).find("[name$='./value']")[0];
                
                searchPropsObject = {
                    name: name.value,
                    value: value.value
                }
                temp.push(searchPropsObject);
            });
            
            if (temp.length > 0) result += 'searchProps=' + JSON.stringify(temp);
        }
        
        return result + '&operation=' + operation.value;
    };
    
});
