import * as Blockly from "blockly";
import * as libraryBlocks from 'blockly/blocks';
import * as En from 'blockly/msg/en';


console.log("hello");

class BlocklyEditor extends HTMLElement {
	constructor() {
		super();
	}
	connectedCallback(){
		const shadow = this.attachShadow({ mode: "open" });
		shadow.innerHTML=`
			<style>
			.blockly{
				height: 480px;
			 width: 600px;
			}
			</style>
			`
		const blocklydiv = document.createElement('div');
		blocklydiv.className="blockly"
		// blocklydiv.style.width = "90vw";
		// 	blocklydiv.style.height = "90vh";
		shadow.appendChild(blocklydiv);
		const testdiv = document.createElement('div');
		testdiv.textContent="watch"
		shadow.appendChild(testdiv)
		const workspace=Blockly.inject(blocklydiv,{})
		const z=workspace.getTopBlocks(true)
		for(let x of z){
			x.getInputTargetBlock('')
		}
	}
}

window.customElements.define("my-elem", BlocklyEditor);
