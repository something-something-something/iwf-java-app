
/**
 * @param {unknown} data
 */
export async function sendDataToDisplay(data){

	const search=new URLSearchParams(window.location.search)
	fetch('/display/submit',{
		method:'POST',
		"headers":{
			"content-type":"application/json"
		},
		body:JSON.stringify({
			displayId:search.get("displayId"),
			workflowId:search.get("workflowId"),
			data
		})
	})
}