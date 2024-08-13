import {
	DirectValue,
	Exit,
	EXIT_TREE_TYPE,
	If,
	IF_TREE_TYPE,
	MAKE_ASYNC_WRITABLE_TREE_TYPE,
	MAKE_DISPLAY_TREE_TYPE,
	MakeAsyncWritable,
	MakeDisplay,
	Print,
	PRINT_TREE_TYPE,
	READ_ASYNC_WRITABLE_TREE_TYPE,
	ReadAsyncWritable,
	ROOT_TREE_TYPE,
	SET_VAR_TREE_TYPE,
	SetVar,
	SIMPLE_OP_TREE_TYPE,
	SimpleOp,
	SortaWhile,
	Statment,
	TreeRoot,
	UPDATE_DISPLAY_TREE_TYPE,
	UpdateDisplay,
	Value,
	Var,
	VAR_TREE_TYPE,
} from './blocklyToTree';

const VAR_ACCESS = 'varAccess';

export type InstructVar = {
	type: typeof VAR_ACCESS;
	name: string;
	path: InstructVal;
};

const DIRECT_VAL = 'directVal';

export type InstructDirVal = {
	type: typeof DIRECT_VAL;
	value: unknown;
};

export type InstructVal = InstructDirVal | InstructVar;

export type Instruct =
	| COPY
	| EXIT
	| LABEL
	| BRANCH
	| MKDISP
	| MKPROMISE
	| AWAIT
	| UPDISP
	| RESPROMISE
	| PRINT
	| ADD
	| MINUS
	| EQ
	| GT
	| LT
	| AND
	| OR
	| DEADEND
	| PARALLEL;

const COPY_TYPE = 'copy';

export type COPY = {
	type: typeof COPY_TYPE;
	arg: InstructVal;
	res: InstructVar;
};

const EXIT_TYPE = 'exit';

export type EXIT = {
	type: typeof EXIT_TYPE;
};

const LABEL_TYPE = 'label';

export type LABEL = {
	type: typeof LABEL_TYPE;
	label: string;
};

const BRANCH_TYPE = 'branch';

export type BRANCH = {
	type: typeof BRANCH_TYPE;
	label: string;
	arg: InstructVal;
};

const MKPROMISE_TYPE = 'mkpromise';

type MKPROMISE = {
	type: typeof MKPROMISE_TYPE;
	res: InstructVar;
};

const AWAIT_TYPE = 'await';

type AWAIT = {
	type: typeof AWAIT_TYPE;
	arg: InstructVar;
	res: InstructVar;
};

const MKDISP_TYPE = 'mkdisp';

type MKDISP = {
	type: typeof MKDISP_TYPE;
	res: InstructVar;
};

const UPDISP_TYPE = 'updisp';

type UPDISP = {
	type: typeof UPDISP_TYPE;
	inputPromiseArg: InstructVal;
	dispIdArg: InstructVal;
	dispArg: InstructVal;
};

const RESPROMISE_TYPE = 'respromise';

type RESPROMISE = {
	type: typeof RESPROMISE_TYPE;
	promiseIdArg: InstructVal;
	valueArg: InstructVal;
};

const PRINT_TYPE = 'print';

type PRINT = {
	type: typeof PRINT_TYPE;
	arg: InstructVal;
};

const ADD_TYPE = 'add';

type ADD = {
	type: typeof ADD_TYPE;
	arg1: InstructVal;
	arg2: InstructVal;
	res: InstructVar;
};

const MINUS_TYPE = 'minus';

type MINUS = {
	type: typeof MINUS_TYPE;
	arg1: InstructVal;
	arg2: InstructVal;
	res: InstructVar;
};

const EQ_TYPE = 'eq';

type EQ = {
	type: typeof EQ_TYPE;
	arg1: InstructVal;
	arg2: InstructVal;
	res: InstructVar;
};

const GT_TYPE = 'gt';

type GT = {
	type: typeof GT_TYPE;
	arg1: InstructVal;
	arg2: InstructVal;
	res: InstructVar;
};

const LT_TYPE = 'lt';

type LT = {
	type: typeof LT_TYPE;
	arg1: InstructVal;
	arg2: InstructVal;
	res: InstructVar;
};

const AND_TYPE = 'and';

type AND = {
	type: typeof AND_TYPE;
	arg1: InstructVal;
	arg2: InstructVal;
	res: InstructVar;
};

const OR_TYPE = 'or';

type OR = {
	type: typeof OR_TYPE;
	arg1: InstructVal;
	arg2: InstructVal;
	res: InstructVar;
};

const DEADEND_TYPE = 'deadend';

type DEADEND = {
	type: typeof DEADEND_TYPE;
};
const PARALLEL_TYPE = 'parallel';

type PARALLEL = {
	type: typeof PARALLEL_TYPE;
	copy: {
		arg: InstructVal;
		res: string;
	}[];
	ref: {
		targetName: string;
		refName: string;
	}[];
};

export function compileTree(t: Statment | TreeRoot): Instruct[] {
	if (t.type === ROOT_TREE_TYPE) {
		return compileRoot(t);
	} else if (t.type === EXIT_TREE_TYPE) {
		return compileExit(t);
	} else if (t.type === PRINT_TREE_TYPE) {
		return compilePrint(t);
	} else if (t.type === SET_VAR_TREE_TYPE) {
		return compileSetVar(t);
	} else if (t.type === SIMPLE_OP_TREE_TYPE) {
		return compileSimpleOp(t);
	} else if (t.type === MAKE_DISPLAY_TREE_TYPE) {
		return compileMakeDisplay(t);
	} else if (t.type === UPDATE_DISPLAY_TREE_TYPE) {
		return compileUpdateDisplay(t);
	} else if (t.type === MAKE_ASYNC_WRITABLE_TREE_TYPE) {
		return compileMakeAsyncWritable(t);
	} else if (t.type === READ_ASYNC_WRITABLE_TREE_TYPE) {
		return compileReadAsyncWritable(t);
	} else if (t.type === IF_TREE_TYPE) {
		return compileIf(t);
	} else {
		return compileSortaWhile(t);
	}
}

function compileSortaWhile(sortaWhile: SortaWhile): Instruct[] {
	const labelForStartLoop = 'startsortawhileloop-' + crypto.randomUUID();
	const labelForCheckIsTrue =
		'labelforconditionalchildren-' + crypto.randomUUID();
	const labelForAfterConditionalChildren = 'finishloop-' + crypto.randomUUID();

	let beforeChildInstructs: Instruct[] = [];
	for (let c of sortaWhile.beforeCheckChildren) {
		beforeChildInstructs = beforeChildInstructs.concat(compileTree(c));
	}

	let childInstructs: Instruct[] = [];
	for (let c of sortaWhile.children) {
		childInstructs = childInstructs.concat(compileTree(c));
	}

	let branchLogic: Instruct[] = [];

	branchLogic = branchLogic
		.concat([{ type: LABEL_TYPE, label: labelForStartLoop }])
		.concat(beforeChildInstructs)
		.concat([
			{
				type: BRANCH_TYPE,
				label: labelForCheckIsTrue,
				arg: compileVal(sortaWhile.check),
			},

			{
				type: BRANCH_TYPE,
				label: labelForAfterConditionalChildren,
				arg: { type: DIRECT_VAL, value: true },
			},
			{ type: LABEL_TYPE, label: labelForCheckIsTrue },
		]);

	branchLogic = branchLogic.concat(childInstructs).concat([
		{
			type: BRANCH_TYPE,
			label: labelForStartLoop,
			arg: { type: DIRECT_VAL, value: true },
		},
	]);

	return branchLogic.concat([
		{
			type: LABEL_TYPE,
			label: labelForAfterConditionalChildren,
		},
	]);
}

function compileIf(theIf: If): Instruct[] {
	const labelForCheckIsTrue = 'checkfortruestatments-' + crypto.randomUUID();
	const labelForAfterConditionalChildren =
		'afterchildren-' + crypto.randomUUID();

	let childInstructs: Instruct[] = [];
	for (let c of theIf.children) {
		childInstructs = childInstructs.concat(compileTree(c));
	}

	let branchLogic: Instruct[] = [
		{
			type: BRANCH_TYPE,
			label: labelForCheckIsTrue,
			arg: compileVal(theIf.check),
		},

		{
			type: BRANCH_TYPE,
			label: labelForAfterConditionalChildren,
			arg: { type: DIRECT_VAL, value: true },
		},
		{ type: LABEL_TYPE, label: labelForCheckIsTrue },
	];

	branchLogic = branchLogic.concat(childInstructs);

	return branchLogic.concat([
		{
			type: LABEL_TYPE,
			label: labelForAfterConditionalChildren,
		},
	]);
}

function compileReadAsyncWritable(raw: ReadAsyncWritable): [AWAIT] {
	return [
		{
			type: AWAIT_TYPE,
			res: compileVar(raw.var),
			arg: compileVar(raw.asyncWritable),
		},
	];
}

function compileMakeAsyncWritable(maw: MakeAsyncWritable): [MKPROMISE] {
	return [
		{
			type: MKPROMISE_TYPE,
			res: compileVar(maw.var),
		},
	];
}

function compileUpdateDisplay(ud: UpdateDisplay): [UPDISP] {
	return [
		{
			type: UPDISP_TYPE,
			dispArg: compileVal(ud.ui),
			dispIdArg: compileVar(ud.display),
			inputPromiseArg: compileVar(ud.asyncWritable),
		},
	];
}

function compileMakeDisplay(md: MakeDisplay): [MKDISP] {
	return [
		{
			type: MKDISP_TYPE,
			res: compileVar(md.var),
		},
	];
}

type SimpleOpInstructions = ADD | MINUS | EQ | GT | LT | AND | OR;

type SimpleOPInstructTypes = SimpleOpInstructions['type'];

const lookUpMap: Map<string, SimpleOPInstructTypes> = new Map([
	['+', ADD_TYPE],
	['-', MINUS_TYPE],
	['==', EQ_TYPE],
	['or', OR_TYPE],
	['and', AND_TYPE],
	['>', GT_TYPE],
	['<', LT_TYPE],
]);

function compileSimpleOp(so: SimpleOp): [SimpleOpInstructions] | never[] {
	const type = lookUpMap.get(so.op);

	if (type === undefined) {
		return [];
	}

	return [
		{
			type,
			arg1: compileVal(so.input1),
			arg2: compileVal(so.input2),
			res: compileVar(so.var),
		},
	];
}

function compileSetVar(sv: SetVar): [COPY] {
	return [
		{
			type: COPY_TYPE,
			arg: compileVal(sv.value),
			res: compileVar(sv.var),
		},
	];
}

function compileExit(et: Exit): [EXIT] {
	return [
		{
			type: EXIT_TYPE,
		},
	];
}

function compileVal(v: Value): InstructVal {
	if (v.type === VAR_TREE_TYPE) {
		return compileVar(v);
	} else {
		return compileDirectValue(v);
	}
}

function compileVar(v: Var): InstructVar {
	return {
		type: VAR_ACCESS,
		name: v.name,
		path: compileVal(v.path),
	};
}

function compileDirectValue(v: DirectValue): InstructDirVal {
	return {
		type: DIRECT_VAL,
		value: v.value,
	};
}

function compilePrint(p: Print): PRINT[] {
	return [
		{
			type: PRINT_TYPE,
			arg: compileVal(p.value),
		},
	];
}

function compileRoot(root: TreeRoot): Instruct[] {
	let instruct: Instruct[] = [];
	for (let c of root.children) {
		instruct = instruct.concat(compileTree(c));
	}
	return instruct;
}
