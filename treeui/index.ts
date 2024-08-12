import * as Blockly from 'blockly';
import * as libraryBlocks from 'blockly/blocks';
import * as En from 'blockly/msg/en';
import { registerFieldMultilineInput } from '@blockly/field-multilineinput';

registerFieldMultilineInput();

console.log('hello');
Blockly.setLocale(En);

Blockly.defineBlocksWithJsonArray([
	{
		type: 'json_value_as_string',
		colour:"#007700",
		message0: 'value %1',
		args0: [
			{
				type: 'field_multilinetext',
				name: 'jsonhere',
			},
		],
		output:['directValue']
	},
	{
		type: 'set_value',
		colour:"#005699",
		message0: 'set %1 to %2 .',
		args0: [
			{
				type: 'input_value',
				name: 'var',
				check:['variable']
			},
			{
				type: 'input_value',
				name: 'value',
				check:['variable','directValue']
			},
		],
		// message1: '= %1',
		// args1: [
		// 	{
		// 		type: 'input_value',
		// 		name: 'value',
		// 		check:['variable','directValue']
		// 	},
		// ],
		previousStatement:null,
		nextStatement:null,
	},
	{
		type: 'if_value_do',
		colour:"#784489",
		message0: 'if ( %1 ) ',
		args0: [

			{
				type: 'input_value',
				name: 'value',
				check:['variable','directValue']
			},
		],
		message1: 'do %1',
		args1: [

			{
				type: 'input_statement',
				name: 'then',
			},
		],
		// message1: '= %1',
		// args1: [
		// 	{
		// 		type: 'input_value',
		// 		name: 'value',
		// 		check:['variable','directValue']
		// 	},
		// ],
		previousStatement:null,
		nextStatement:null,
	},
	{
		type: 'sorta_a_while',
		colour:"#ff4930",
		message0: 'before check %1',
		args0: [

			{
				type: 'input_statement',
				name: 'beforecheck',
			},
		],
		message1: 'check %1',
		args1: [

			{
				type: 'input_value',
				name: 'value',
				check:['variable','directValue']
			},
		],
		message2: 'do %1 \n repeat',
		args2: [

			{
				type: 'input_statement',
				name: 'then',
				
			},
		],
		// message1: '= %1',
		// args1: [
		// 	{
		// 		type: 'input_value',
		// 		name: 'value',
		// 		check:['variable','directValue']
		// 	},
		// ],
		previousStatement:null,
		nextStatement:null,
	},
	{
		type: 'variable',
		colour:"#239239",
		message0: 'varname %1 path: %2',
		args0: [
			{
				type: 'field_input',
				name: 'varname',
			},
			{
				type: 'input_value',
				name: 'path',
				check:['directValue','variable']
			},
		],
		output:['variable']
	},
]);

class BlocklyEditor extends HTMLElement {
	constructor() {
		super();
	}
	connectedCallback() {
		//const shadow = this.attachShadow({ mode: "open" });
		// shadow.innerHTML=`
		// 	<style>
		// 	.blockly{
		// 		height: 480px;
		// 	 width: 600px;
		// 	}
		// 	</style>
		// 	`
		const blocklydiv = document.createElement('div');
		blocklydiv.className = 'blockly';
		blocklydiv.style.width = '99vw';
		blocklydiv.style.height = '99vh';
		this.appendChild(blocklydiv);
		const testdiv = document.createElement('div');
		testdiv.textContent = 'watch';
		//shadow.appendChild(testdiv)
		const workspace = Blockly.inject(blocklydiv, {
			move:{
				scrollbars:true,
				drag:true,
				wheel:true
			},
			zoom:{
				controls: true,
				startScale: 1.0,
				pinch: true
			},
			toolbox: {
				kind: 'flyoutToolbox',
				contents: [
					{
						kind: 'block',
						type: 'json_value_as_string',
					},
					{
						kind: 'block',
						type: 'variable',
					},
					{
						kind:'block',
						type:'set_value'
					},
					{
						kind:'block',
						type:'if_value_do'
					},
					{
						kind:'block',
						type:'sorta_a_while'
					}
				],
			},
		});
		const z = workspace.getTopBlocks(true);
		for (let x of z) {
			x.getInputTargetBlock('');
		}
	}
}

window.customElements.define('my-elem', BlocklyEditor);
