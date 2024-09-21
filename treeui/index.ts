import * as Blockly from 'blockly';
//import * as libraryBlocks from 'blockly/blocks';
import * as En from 'blockly/msg/en';
import { registerFieldMultilineInput } from '@blockly/field-multilineinput';
import { getTree } from './blocklyToTree';
import { compileTree, Instruct } from './compiler';

registerFieldMultilineInput();

Blockly.setLocale(En);

const STORAGE_PREFIX = 'BLOCKLY_EDITOR_PREFIX:';

export const VALUE_BLOCK_TYPE = 'json_value_as_string';
export const SET_VALUE_BLOCK_TYPE = 'set_value';
export const UPDATE_DISPLAY_BLOCK_TYPE = 'update_display';
export const MAKE_DISPLAY_BLOCK_TYPE = 'make_display';
export const VARIABLE_BLOCK_TYPE_BLOCK_TYPE = 'variable';
export const READ_ASYNC_WRITABLE_BLOCK_TYPE = 'read_async_writable';
export const MAKE_ASYNC_WRITABLE_BLOCK_TYPE = 'make_async_writable';
export const PRINT_BLOCK_TYPE = 'print';
export const IF_BLOCK_TYPE = 'if_value_do';
export const SORTA_WHILE_BLOCK_TYPE = 'sorta_a_while';
export const SIMPLE_OP_BLOCK_TYPE = 'simple_op';
export const EXIT_BLOCK_TYPE = 'exit';
export const FUNC_CALL_BLOCK_TYPE = 'func_call';
export const FUNC_DEFINE_BLOCK_TYPE = 'func_define';
export const FUNC_RETURN_BLOCK_TYPE = 'func_return';
export const FUNC_ARG_COPY_BLOCK_TYPE = 'copyArg';
export const FUNC_ARG_REF_BLOCK_TYPE = 'func_ref_arg';

const STATMENT_CONNECTION = ['statment'] as const;

Blockly.defineBlocksWithJsonArray([
	{
		type: VALUE_BLOCK_TYPE,
		colour: '#007700',
		message0: 'value %1',
		args0: [
			{
				type: 'field_multilinetext',
				name: 'jsonhere',
				spellcheck: false,
			},
		],
		output: ['directValue'],
	},
	{
		type: SET_VALUE_BLOCK_TYPE,
		colour: '#005699',
		message0: 'set %1 to %2 .',
		args0: [
			{
				type: 'input_value',
				name: 'var',
				check: ['variable'],
			},
			{
				type: 'input_value',
				name: 'value',
				check: ['variable', 'directValue'],
			},
		],
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},
	{
		type: MAKE_DISPLAY_BLOCK_TYPE,
		colour: '#005699',
		message0: 'set %1 to new display .',
		args0: [
			{
				type: 'input_value',
				name: 'var',
				check: ['variable'],
			},
		],
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},
	{
		type: UPDATE_DISPLAY_BLOCK_TYPE,
		colour: '#005699',
		message0: 'set display %1 to ui %2 .\n',
		args0: [
			{
				type: 'input_value',
				name: 'display',
				check: ['variable'],
			},
			{
				type: 'input_value',
				name: 'ui',
				check: ['variable', 'directValue'],
			},
		],
		message1: 'when input place in  %1 .',
		args1: [
			{
				type: 'input_value',
				name: 'async_writable',
				check: ['variable'],
			},
		],
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},
	{
		type: MAKE_ASYNC_WRITABLE_BLOCK_TYPE,
		colour: '#005699',
		message0: 'set %1 to new async writable .',
		args0: [
			{
				type: 'input_value',
				name: 'var',
				check: ['variable'],
			},
		],
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},
	{
		type: READ_ASYNC_WRITABLE_BLOCK_TYPE,
		colour: '#005699',
		message0: 'set %1 to result of %2 when done.',
		args0: [
			{
				type: 'input_value',
				name: 'var',
				check: ['variable'],
			},
			{
				type: 'input_value',
				name: 'async_writable',
				check: ['variable'],
			},
		],
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},
	{
		type: SIMPLE_OP_BLOCK_TYPE,
		colour: '#7777ff',
		message0: 'set %1 to ( %2 %3 %4)',
		args0: [
			{
				type: 'input_value',
				name: 'var',
				check: ['variable'],
			},
			{
				type: 'input_value',
				name: 'input1',
				check: ['variable', 'directValue'],
			},
			{
				type: 'field_dropdown',
				name: 'op',
				options: [
					['+', '+'],
					['-', '-'],
					['==', '=='],
					['and', 'and'],
					['or', 'or'],
					['>', '>'],
					['<', '<'],
				],
			},
			{
				type: 'input_value',
				name: 'input2',
				check: ['variable', 'directValue'],
			},
		],
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},
	{
		type: PRINT_BLOCK_TYPE,
		colour: '#0056ff',
		message0: 'print %1 .',
		args0: [
			{
				type: 'input_value',
				name: 'print',
				check: ['variable', 'directValue'],
			},
		],
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},
	{
		type: IF_BLOCK_TYPE,
		colour: '#784489',
		message0: 'if ( %1 ) ',
		args0: [
			{
				type: 'input_value',
				name: 'value',
				check: ['variable', 'directValue'],
			},
		],
		message1: 'do %1',
		args1: [
			{
				type: 'input_statement',
				name: 'then',
				check: STATMENT_CONNECTION,
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
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},
	{
		type: EXIT_BLOCK_TYPE,
		colour: '#ff0000',
		message0: 'exit',
		args0: [],
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},
	{
		type: SORTA_WHILE_BLOCK_TYPE,
		colour: '#774977',
		message0: 'before check %1',
		args0: [
			{
				type: 'input_statement',
				name: 'beforecheck',
				check: STATMENT_CONNECTION,
			},
		],
		message1: 'check %1',
		args1: [
			{
				type: 'input_value',
				name: 'value',
				check: ['variable', 'directValue'],
			},
		],
		message2: 'do %1 \n repeat',
		args2: [
			{
				type: 'input_statement',
				name: 'then',
				check: STATMENT_CONNECTION,
			},
		],
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},

	{
		type: FUNC_CALL_BLOCK_TYPE,
		colour: '#990099',
		message0: 'set %1 to result func %2 with args: copy %3 ref %4',
		args0: [
			{
				type: 'input_value',
				name: 'resultVar',
				check: ['variable'],
			},

			{
				type: 'field_input',
				name: 'funcName',
			},
			{
				type: 'input_statement',
				name: 'copyArgs',
				check: ['copyArg'],
			},
			{
				type: 'input_statement',
				name: 'refArgs',
				check: ['refArg'],
			},
		],
		message1: 'async %1',
		args1: [
			{
				type: 'field_checkbox',
				name: 'isAsync',
				checked: false,
			},
		],
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},
	{
		type: FUNC_ARG_COPY_BLOCK_TYPE,
		colour: '#990099',
		message0: 'copy %1 to %2 in function scope',
		args0: [
			{
				type: 'input_value',
				name: 'value',
				check: ['variable', 'directValue'],
			},
			{
				type: 'field_input',
				name: 'varNameInFunc',
			},
		],
		previousStatement: ['copyArg'],
		nextStatement: ['copyArg'],
	},

	{
		type: FUNC_ARG_REF_BLOCK_TYPE,
		colour: '#990099',
		message0: 'link %1 to %2 in function scope',
		args0: [
			{
				type: 'field_input',
				name: 'varNameInCurrentScope',
			},
			{
				type: 'field_input',
				name: 'varNameInFunc',
			},
		],
		previousStatement: ['refArg'],
		nextStatement: ['refArg'],
	},
	{
		type: FUNC_DEFINE_BLOCK_TYPE,
		colour: '#990099',
		message0: 'func %1 %2',
		args0: [
			{
				type: 'field_input',
				name: 'funcName',
			},
			{
				type: 'input_statement',
				name: 'children',
				check: STATMENT_CONNECTION,
			},
		],
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},

	{
		type: FUNC_RETURN_BLOCK_TYPE,
		colour: '#990099',
		message0: 'return %1',
		args0: [
			{
				type: 'input_value',
				name: 'value',
				check: ['variable', 'directValue'],
			},
		],
		previousStatement: STATMENT_CONNECTION,
		nextStatement: STATMENT_CONNECTION,
	},

	{
		type: VARIABLE_BLOCK_TYPE_BLOCK_TYPE,
		colour: '#239239',
		message0: 'varname %1 path: %2',
		args0: [
			{
				type: 'field_input',
				name: 'varname',
			},
			{
				type: 'input_value',
				name: 'path',
				check: ['directValue', 'variable'],
			},
		],
		output: ['variable'],
	},
]);

class BlocklyEditor extends HTMLElement {
	#blocklyWorkspace: Blockly.WorkspaceSvg | null = null;

	constructor() {
		super();
	}
	connectedCallback() {
		const shadow = this.attachShadow({ mode: 'open' });

		const mainslot = document.createElement('slot');
		shadow.appendChild(mainslot);
		const buttonCompile = document.createElement('button');
		buttonCompile.classList.add('compile-button');
		buttonCompile.textContent = 'compile';
		shadow.appendChild(buttonCompile);
		// shadow.innerHTML=`
		// 	<style>
		// 	.blockly{
		// 		height: 480px;
		// 	 width: 600px;
		// 	}
		// 	</style>
		// 	`
		const buttonSave = document.createElement('button');
		buttonSave.classList.add('save-button');
		buttonSave.textContent = 'save';
		shadow.appendChild(buttonSave);

		const inputSave = document.createElement('input');
		inputSave.type = 'text';
		inputSave.classList.add('save-input');
		shadow.appendChild(inputSave);

		const buttonLoad = document.createElement('button');
		buttonLoad.classList.add('load-button');
		buttonLoad.textContent = 'load';
		shadow.appendChild(buttonLoad);

		const inputLoad = document.createElement('input');
		inputLoad.type = 'text';
		inputLoad.classList.add('load-input');
		shadow.appendChild(inputLoad);

		const buttonExportFile = document.createElement('button');
		buttonExportFile.classList.add('export-button');
		buttonExportFile.textContent = 'Export';
		buttonExportFile.addEventListener('click', (ev) => {
			this.serializeToFile();
		});
		shadow.appendChild(buttonExportFile);

		const importFileLabel = document.createElement('label');
		importFileLabel.textContent = 'Import';
		const inputImportFile = document.createElement('input');
		inputImportFile.type = 'file';
		inputImportFile.classList.add('import-button');
		inputImportFile.style.display = 'none';
		inputImportFile.addEventListener('change', (ev) => {
			if (ev.target instanceof HTMLInputElement) {
				this.deserializeFromFileInput(ev.target);
			}
		});

		importFileLabel.appendChild(inputImportFile);
		shadow.appendChild(importFileLabel);

		const resultDialog = document.createElement('dialog');
		resultDialog.classList.add('compile-result-dialog');
		shadow.appendChild(resultDialog);

		const resultsummary = document.createElement('button');
		resultsummary.classList.add('show-compile-result');
		resultsummary.textContent = 'compile results';
		shadow.appendChild(resultsummary);

		const dialogForm = document.createElement('form');
		dialogForm.method = 'dialog';
		const dialogformclosebutton = document.createElement('button');
		dialogformclosebutton.textContent = 'close dialog';
		dialogForm.appendChild(dialogformclosebutton);

		resultDialog.appendChild(dialogForm);

		const resultDiv = document.createElement('div');
		resultDiv.classList.add('compile-result');
		resultDialog.appendChild(resultDiv);

		const blocklydiv = document.createElement('div');
		blocklydiv.className = 'blockly';
		blocklydiv.style.width = '99vw';
		blocklydiv.style.height = 'calc( 99vh - 3rem)';
		this.appendChild(blocklydiv);
		const testdiv = document.createElement('div');
		testdiv.textContent = 'watch';
		//shadow.appendChild(testdiv)
		const workspace = Blockly.inject(blocklydiv, {
			sounds: false,
			move: {
				scrollbars: true,
				drag: true,
				wheel: true,
			},
			zoom: {
				controls: true,
				startScale: 1.0,
				pinch: true,
			},
			toolbox: {
				kind: 'flyoutToolbox',
				contents: [
					{
						kind: 'block',
						type: VALUE_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: VARIABLE_BLOCK_TYPE_BLOCK_TYPE,
						inputs: {
							path: {
								block: {
									type: VALUE_BLOCK_TYPE,
									fields: {
										jsonhere: '[]',
									},
								},
							},
						},
					},
					{
						kind: 'block',
						type: SET_VALUE_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: PRINT_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: SIMPLE_OP_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: MAKE_DISPLAY_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: UPDATE_DISPLAY_BLOCK_TYPE,
						inputs: {
							ui: {
								block: {
									type: 'json_value_as_string',
									fields: {
										jsonhere: JSON.stringify(
											{
												type: 'customElement',
												elementName: 'choice-options',
												module: '/js/choices.js',
												attributes: {
													'data-describe': 'Choose a or b',
												},
												properties: {
													choices: [
														{
															value: 'a',
															humanText: 'A',
														},
														{
															value: 'b',
															humanText: 'B',
														},
													],
												},
											},
											null,
											'    ',
										),
									},
								},
							},
						},
					},
					{
						kind: 'block',
						type: MAKE_ASYNC_WRITABLE_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: READ_ASYNC_WRITABLE_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: IF_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: SORTA_WHILE_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: EXIT_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: FUNC_DEFINE_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: FUNC_CALL_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: FUNC_ARG_COPY_BLOCK_TYPE,
					},
					{
						kind: 'block',
						type: FUNC_ARG_REF_BLOCK_TYPE,
					},

					{
						kind: 'block',
						type: FUNC_RETURN_BLOCK_TYPE,
					},
				],
			},
		});
		this.#blocklyWorkspace = workspace;
		this.addEventListener('click', (ev) => {
			const composedPath = ev.composedPath();
			for (const target of composedPath) {
				if (target instanceof HTMLElement) {
					if (target.classList.contains('compile-button')) {
						console.log('compile test');
						const result = this.compile();

						const resultelem = shadow.querySelector('.compile-result');
						if (resultelem !== null) {
							resultelem.innerHTML = '';
							for (const instructObj of result) {
								const resultTextArea = document.createElement('textarea');

								resultTextArea.value = JSON.stringify(instructObj, null, '\t');
								resultTextArea.style.minWidth = '60vw';
								resultTextArea.style.minHeight = '60vh';
								resultTextArea.style.display = 'block';
								resultelem.appendChild(resultTextArea);
								this.showCompileResult();
							}
						}
					} else if (target.classList.contains('save-button')) {
						const input = shadow.querySelector('.save-input');
						if (input !== null && input instanceof HTMLInputElement) {
							this.save(input.value);
						}
					} else if (target.classList.contains('load-button')) {
						const input = shadow.querySelector('.load-input');
						if (input !== null && input instanceof HTMLInputElement) {
							this.load(input.value);
						}
					} else if (target.classList.contains('show-compile-result')) {
						this.showCompileResult();
					}
				}
			}
		});
	}

	showCompileResult() {
		const shadow = this.shadowRoot;
		if (shadow !== null) {
			const dialog = shadow.querySelector('.compile-result-dialog');
			console.log(dialog);
			if (dialog instanceof HTMLDialogElement) {
				dialog.showModal();
			}
		}
	}

	save(key: string) {
		if (this.#blocklyWorkspace !== null) {
			const workspace = Blockly.serialization.workspaces.save(
				this.#blocklyWorkspace,
			);
			localStorage.setItem(STORAGE_PREFIX + key, JSON.stringify(workspace));
		}
	}

	load(key: string) {
		if (this.#blocklyWorkspace !== null) {
			const storedState = localStorage.getItem(STORAGE_PREFIX + key);
			if (storedState !== null) {
				const workspace = JSON.parse(storedState);
				Blockly.serialization.workspaces.load(
					workspace,
					this.#blocklyWorkspace,
				);
			}
		}
	}
	compile() {
		console.log('compile');
		if (this.#blocklyWorkspace !== null) {
			console.log('compile start');
			const trees = getTree(this.#blocklyWorkspace);
			const programs: { instructions: Instruct[] }[] = [];
			for (const tree of trees) {
				console.log(tree);
				const instructions = compileTree(tree);
				programs.push({ instructions });
			}
			return programs;
		}
		console.log('compile end');
		return [];
	}

	serializeBlockly() {
		if (this.#blocklyWorkspace !== null) {
			return JSON.stringify(
				Blockly.serialization.workspaces.save(this.#blocklyWorkspace),
			);
		}
	}

	serializeToFile() {
		const str = this.serializeBlockly();
		if (str !== undefined) {
			const file = new File([str], 'export.json', { type: 'application/json' });

			const url = URL.createObjectURL(file);
			window.open(url);
		}
	}

	deserializeBlockly(state: Record<string, unknown>) {
		if (this.#blocklyWorkspace !== null) {
			Blockly.serialization.workspaces.load(state, this.#blocklyWorkspace);
		}
	}

	async deserializeFromFileInput(el: HTMLInputElement) {
		if (el.files !== null) {
			let file = el.files[0];
			const filecontents = await file.text();
			this.deserializeBlockly(JSON.parse(filecontents));
		}
	}
}

window.customElements.define('my-elem', BlocklyEditor);
