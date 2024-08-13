import * as Blockly from 'blockly';
import {
	EXIT_BLOCK_TYPE,
	IF_BLOCK_TYPE,
	MAKE_ASYNC_WRITABLE_BLOCK_TYPE,
	MAKE_DISPLAY_BLOCK_TYPE,
	PRINT_BLOCK_TYPE,
	READ_ASYNC_WRITABLE_BLOCK_TYPE,
	SET_VALUE_BLOCK_TYPE,
	SIMPLE_OP_BLOCK_TYPE,
	SORTA_WHILE_BLOCK_TYPE,
	UPDATE_DISPLAY_BLOCK_TYPE,
	VALUE_BLOCK_TYPE,
	VARIABLE_BLOCK_TYPE_BLOCK_TYPE,
} from './index';

export function getTree(w: Blockly.WorkspaceSvg): TreeRoot[] {
	const topBlocks = w.getTopBlocks();
	const roots: TreeRoot[] = [];
	for (let block of topBlocks) {
		let statments: Statment[] = [];

		for (
			let curBlock: Blockly.BlockSvg | null = block;
			curBlock !== null;
			curBlock = curBlock.getNextBlock()
		) {
			try {
				const tree = intoTree(curBlock);
				if (isStatment(tree)) {
					statments.push(tree);
				} else {
					console.error(
						'not a statment but is direct child of root skipping' +
							curBlock.toString(),
					);
				}
			} catch (err) {
				console.error(err);
			}
		}
		roots.push({
			type: ROOT_TREE_TYPE,
			children: statments,
		});
	}
	return roots;
}

export type SyntaxTree = TreeRoot | Statment | Value;
export const ROOT_TREE_TYPE = 'root';

export type TreeRoot = {
	type: typeof ROOT_TREE_TYPE;
	children: Statment[];
};
export type Statment =
	| If
	| Exit
	| Print
	| SetVar
	| SimpleOp
	| SortaWhile
	| MakeDisplay
	| UpdateDisplay
	| MakeAsyncWritable
	| ReadAsyncWritable;

export type Value = Var | DirectValue;
export const EXIT_TREE_TYPE = 'exit';

export type Exit = { type: typeof EXIT_TREE_TYPE };

export const PRINT_TREE_TYPE = 'print';

export type Print = { type: typeof PRINT_TREE_TYPE; value: Value };

export const MAKE_DISPLAY_TREE_TYPE = 'makeDisplay';

export type MakeDisplay = { type: typeof MAKE_DISPLAY_TREE_TYPE; var: Var };

export const UPDATE_DISPLAY_TREE_TYPE = 'updateDisplay';

export type UpdateDisplay = {
	type: typeof UPDATE_DISPLAY_TREE_TYPE;
	display: Var;
	ui: Value;
	asyncWritable: Var;
};

export const MAKE_ASYNC_WRITABLE_TREE_TYPE = 'makeAsyncWritable';

export type MakeAsyncWritable = {
	type: typeof MAKE_ASYNC_WRITABLE_TREE_TYPE;
	var: Var;
};

export const IF_TREE_TYPE = 'if';

export type If = {
	type: typeof IF_TREE_TYPE;
	check: Value;
	children: Statment[];
};

export const SORTA_WHILE_TREE_TYPE = 'sortaWhile';

export type SortaWhile = {
	type: typeof SORTA_WHILE_TREE_TYPE;
	beforeCheckChildren: Statment[];
	check: Value;
	children: Statment[];
};

export const READ_ASYNC_WRITABLE_TREE_TYPE = 'readAsyncWritable';

export type ReadAsyncWritable = {
	type: typeof READ_ASYNC_WRITABLE_TREE_TYPE;
	var: Var;
	asyncWritable: Var;
};

export const SET_VAR_TREE_TYPE = 'setVar';

export type SetVar = { type: typeof SET_VAR_TREE_TYPE; var: Var; value: Value };

export const SIMPLE_OP_TREE_TYPE = 'simpleOp';

export type SimpleOp = {
	type: typeof SIMPLE_OP_TREE_TYPE;
	var: Var;
	op: string;
	input1: Value;
	input2: Value;
};

export const DIRECT_VALUE_TREE_TYPE = 'directValue';

export type DirectValue = {
	type: typeof DIRECT_VALUE_TREE_TYPE;
	value: unknown;
};

export const VAR_TREE_TYPE = 'var';

export type Var = { type: typeof VAR_TREE_TYPE; name: string; path: Value };

export function isValue(s: SyntaxTree) {
	return s.type === VAR_TREE_TYPE || s.type === DIRECT_VALUE_TREE_TYPE;
}

export function isStatment(s: SyntaxTree) {
	return (
		s.type !== VAR_TREE_TYPE &&
		s.type !== DIRECT_VALUE_TREE_TYPE &&
		s.type !== ROOT_TREE_TYPE
	);
}

export function intoTree(b: Blockly.Block): SyntaxTree {
	if (b.type === VALUE_BLOCK_TYPE) {
		const z = b.getField('jsonhere')?.getText();
		if (z !== undefined) {
			try {
				return {
					type: 'directValue',
					value: JSON.parse(z),
				};
			} catch (err) {
				throw new Error('error in: ' + b.toString(), { cause: err });
			}
		}
	} else if (b.type === VARIABLE_BLOCK_TYPE_BLOCK_TYPE) {
		const varName = b.getField('varname')?.getText();
		let resolvedPath: Value = {
			type: 'directValue',
			value: [],
		};
		const path = b.getInputTargetBlock('path');
		if (path !== null) {
			const tree = intoTree(path);
			if (isValue(tree)) {
				resolvedPath = tree;
			}
		}
		if (varName !== undefined) {
			return {
				type: VAR_TREE_TYPE,
				name: varName,
				path: resolvedPath,
			};
		}
	} else if (b.type === EXIT_BLOCK_TYPE) {
		return {
			type: EXIT_TREE_TYPE,
		};
	} else if (b.type === PRINT_BLOCK_TYPE) {
		const blocktoPrint = b.getInputTargetBlock('print');

		if (blocktoPrint !== null) {
			const value = intoTree(blocktoPrint);

			if (isValue(value)) {
				return {
					type: PRINT_TREE_TYPE,
					value: value,
				};
			}
		}
	} else if (b.type === SET_VALUE_BLOCK_TYPE) {
		const theVarBlock = b.getInputTargetBlock('var');
		const theValueBlock = b.getInputTargetBlock('value');
		if (theVarBlock !== null && theValueBlock !== null) {
			const varTree = intoTree(theVarBlock);
			const valTree = intoTree(theValueBlock);
			if (varTree.type === VAR_TREE_TYPE && isValue(valTree))
				return {
					type: SET_VAR_TREE_TYPE,
					var: varTree,
					value: valTree,
				};
		}
	} else if (b.type === MAKE_ASYNC_WRITABLE_BLOCK_TYPE) {
		const theVarBlock = b.getInputTargetBlock('var');
		if (theVarBlock !== null) {
			const varTree = intoTree(theVarBlock);
			if (varTree.type === VAR_TREE_TYPE) {
				return {
					type: MAKE_ASYNC_WRITABLE_TREE_TYPE,
					var: varTree,
				};
			}
		}
	} else if (b.type === READ_ASYNC_WRITABLE_BLOCK_TYPE) {
		const theVarBlock = b.getInputTargetBlock('var');
		const theAsyncWritableBlock = b.getInputTargetBlock('async_writable');
		if (theVarBlock !== null && theAsyncWritableBlock !== null) {
			const varTree = intoTree(theVarBlock);
			const theAsyncWritableTree = intoTree(theAsyncWritableBlock);
			if (
				varTree.type === VAR_TREE_TYPE &&
				theAsyncWritableTree.type === VAR_TREE_TYPE
			) {
				return {
					type: READ_ASYNC_WRITABLE_TREE_TYPE,
					var: varTree,
					asyncWritable: theAsyncWritableTree,
				};
			}
		}
	} else if (b.type === MAKE_DISPLAY_BLOCK_TYPE) {
		const theVarBlock = b.getInputTargetBlock('var');
		if (theVarBlock !== null) {
			const varTree = intoTree(theVarBlock);
			if (varTree.type === VAR_TREE_TYPE) {
				return {
					type: MAKE_DISPLAY_TREE_TYPE,
					var: varTree,
				};
			}
		}
	} else if (b.type === UPDATE_DISPLAY_BLOCK_TYPE) {
		const displayBlock = b.getInputTargetBlock('display');
		const theAsyncWritableBlock = b.getInputTargetBlock('async_writable');
		const uiBlock = b.getInputTargetBlock('ui');
		if (
			displayBlock !== null &&
			theAsyncWritableBlock !== null &&
			uiBlock !== null
		) {
			const displayTree = intoTree(displayBlock);
			const theAsyncWritableTree = intoTree(theAsyncWritableBlock);
			const uiTree = intoTree(uiBlock);
			if (
				displayTree.type === VAR_TREE_TYPE &&
				theAsyncWritableTree.type === VAR_TREE_TYPE &&
				isValue(uiTree)
			) {
				return {
					type: UPDATE_DISPLAY_TREE_TYPE,
					ui: uiTree,
					display: displayTree,
					asyncWritable: theAsyncWritableTree,
				};
			}
		}
	} else if (b.type === SIMPLE_OP_BLOCK_TYPE) {
		const theVarBlock = b.getInputTargetBlock('var');
		const theValue1Block = b.getInputTargetBlock('input1');
		const theValue2Block = b.getInputTargetBlock('input2');
		const op = b.getField('op')?.getText();
		if (
			theVarBlock !== null &&
			theValue1Block !== null &&
			theValue2Block !== null
		) {
			const varTree = intoTree(theVarBlock);
			const val1Tree = intoTree(theValue1Block);
			const val2Tree = intoTree(theValue2Block);
			if (
				varTree.type === VAR_TREE_TYPE &&
				isValue(val1Tree) &&
				isValue(val2Tree) &&
				op !== undefined
			) {
				return {
					type: SIMPLE_OP_TREE_TYPE,
					var: varTree,
					input1: val1Tree,
					input2: val2Tree,
					op: op,
				};
			}
		}
	} else if (b.type === IF_BLOCK_TYPE) {
		const valueBlock = b.getInputTargetBlock('value');
		const theThenBlock = b.getInputTargetBlock('then');
		if (valueBlock !== null) {
			const thenTreeList: Statment[] = [];
			for (
				let curBlock = theThenBlock;
				curBlock !== null;
				curBlock = curBlock.getNextBlock()
			) {
				const thenTree = intoTree(curBlock);
				if (isStatment(thenTree)) {
					thenTreeList.push(thenTree);
				} else {
					throw new Error(
						'block not stament in if:' +
							b.toString() +
							' child: ' +
							curBlock.toString(),
					);
				}
			}

			const valTree = intoTree(valueBlock);
			if (isValue(valTree)) {
				return {
					type: IF_TREE_TYPE,
					check: valTree,
					children: thenTreeList,
				};
			}
		}
	} else if (b.type === SORTA_WHILE_BLOCK_TYPE) {
		const valueBlock = b.getInputTargetBlock('value');
		const theBeforeCheckBlock = b.getInputTargetBlock('beforecheck');
		const theThenBlock = b.getInputTargetBlock('then');
		if (valueBlock !== null) {
			const thenTreeList: Statment[] = [];
			for (
				let curBlock = theThenBlock;
				curBlock !== null;
				curBlock = curBlock.getNextBlock()
			) {
				const thenTree = intoTree(curBlock);
				if (isStatment(thenTree)) {
					thenTreeList.push(thenTree);
				} else {
					throw new Error(
						'block not stament in while:' +
							b.toString() +
							' child: ' +
							curBlock.toString(),
					);
				}
			}

			const beforCheckList: Statment[] = [];
			for (
				let curBlock = theBeforeCheckBlock;
				curBlock !== null;
				curBlock = curBlock.getNextBlock()
			) {
				const beforeTree = intoTree(curBlock);
				if (isStatment(beforeTree)) {
					beforCheckList.push(beforeTree);
				} else {
					throw new Error(
						'block not stament in while:' +
							b.toString() +
							' child: ' +
							curBlock.toString(),
					);
				}
			}
			const valTree = intoTree(valueBlock);
			if (isValue(valTree)) {
				return {
					type: SORTA_WHILE_TREE_TYPE,
					beforeCheckChildren: beforCheckList,
					check: valTree,
					children: thenTreeList,
				};
			}
		}
	}

	throw new Error('not recognized: ' + b.toString());
}
