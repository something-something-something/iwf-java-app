package ekrebs.iwf.web.exec;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Exec {

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({
			@Type(value = Data.VariableAccess.class, name = "varAccess"),
			@Type(value = Data.DirectValue.class, name = "directVal")
	})
	sealed interface Data {
		record VariableAccess(String name, Data path) implements Data {
		};

		record DirectValue(JsonNode value) implements Data {
		};

	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({
			@Type(value = Instruction.Copy.class, name = "copy"),
			@Type(value = Instruction.Print.class, name = "print"),
			@Type(value = Instruction.Exit.class, name = "exit"),
			@Type(value = Instruction.Label.class, name = "label"),
			@Type(value = Instruction.Branch.class, name = "branch"),
			@Type(value = Instruction.MakePromise.class, name = "mkpromise"),
			@Type(value = Instruction.AwaitPromise.class, name = "await"),

			@Type(value = Instruction.MakeDisplay.class, name = "mkdisp"),
			@Type(value = Instruction.UpdateDisplay.class, name = "updisp"),
			@Type(value = Instruction.Add.class, name = "add"),
			@Type(value = Instruction.Minus.class, name = "minus"),

			@Type(value = Instruction.Eq.class, name = "eq"),
			@Type(value = Instruction.Gt.class, name = "gt"),
			@Type(value = Instruction.Lt.class, name = "lt"),
			@Type(value = Instruction.Or.class, name = "or"),
			@Type(value = Instruction.And.class, name = "and"),
			@Type(value = Instruction.Parallel.class, name = "parallel"),
			@Type(value = Instruction.DeadEnd.class, name = "deadend"),
			@Type(value = Instruction.ResPromise.class, name = "respromise"),
	})
	public sealed interface Instruction {
		<T> T accept(InstructionVisitor<T> visitor);

		record Copy(Data arg, Data.VariableAccess res) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record Exit() implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record Print(Data arg) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record Label(String label) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record Branch(Data arg, String label) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record MakePromise(Data.VariableAccess res) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record AwaitPromise(Data arg, Data.VariableAccess res) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record MakeDisplay(Data.VariableAccess res) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record UpdateDisplay(Data inputPromiseArg, Data dispArg, Data dispIdArg) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record Add(Data arg1, Data arg2, Data.VariableAccess res) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record Minus(Data arg1, Data arg2, Data.VariableAccess res) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record Eq(Data arg1, Data arg2, Data.VariableAccess res) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record Gt(Data arg1, Data arg2, Data.VariableAccess res) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record Lt(Data arg1, Data arg2, Data.VariableAccess res) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record And(Data arg1, Data arg2, Data.VariableAccess res) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record Or(Data arg1, Data arg2, Data.VariableAccess res) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record Parallel(String label,List<CopyItem> copy, List<RefItem> ref) implements Instruction {

			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
			public record CopyItem(Data arg, String res) {
			}

			public record RefItem(String targetName, String refName) {
			}
		}

		record DeadEnd() implements Instruction {
			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

		record ResPromise(Data promiseIdArg, Data valueArg) implements Instruction {
			@Override
			public <T> T accept(InstructionVisitor<T> v) {
				return v.vist(this);
			}
		}

	}

	interface InstructionVisitor<T> {

		T vist(Instruction.Copy inst);

		T vist(Instruction.Exit inst);

		T vist(Instruction.Print inst);

		T vist(Instruction.Label inst);

		T vist(Instruction.Branch inst);

		T vist(Instruction.MakePromise inst);

		T vist(Instruction.AwaitPromise inst);

		T vist(Instruction.MakeDisplay inst);

		T vist(Instruction.UpdateDisplay inst);

		T vist(Instruction.Add inst);

		T vist(Instruction.Minus inst);

		T vist(Instruction.Eq inst);

		T vist(Instruction.Gt inst);

		T vist(Instruction.Lt inst);

		T vist(Instruction.And inst);

		T vist(Instruction.Or inst);

		T vist(Instruction.Parallel inst);

		T vist(Instruction.DeadEnd inst);

		T vist(Instruction.ResPromise inst);

	}

	public record ParseResult(List<Instruction> instructions, List<ParseError> errors,
			Map<String, Integer> labeledBookmarks) {
	}

	record ParseError(List<Object> path, String message) {
	}

	public static ParseResult parse(JsonNode jsonNode) {
		var labeledBookmarks = new HashMap<String, Integer>();
		var instructions = new ArrayList<Instruction>();
		var errs = new ArrayList<ParseError>();
		var results = new ParseResult(instructions, errs, labeledBookmarks);
		if (!jsonNode.isObject()) {
			errs.add(new ParseError(Arrays.asList(), "Root is not an object"));
			return results;
		}

		var rootObject = (ObjectNode) jsonNode;

		var instructionsProp = rootObject.path("instructions");
		if (instructionsProp.isMissingNode()) {
			errs.add(new ParseError(Arrays.asList(), "no instructions found"));
			return results;
		}
		if (!instructionsProp.isArray()) {
			errs.add(new ParseError(iep(), "no instructions found"));
			return results;
		}

		var instructionsList = (ArrayNode) instructionsProp;

		for (var i = 0; i < instructionsList.size(); i++) {
			var instNode = instructionsList.path(i);
			var typePath = instNode.path("type");
			if (typePath.isMissingNode()) {
				errs.add(new ParseError(iep(i), "Missing type"));
				instructions.add(new Instruction.Exit());
			} else {
				var parsedInstruction = parseInstruction(instNode, i, labeledBookmarks);

				instructions.add(parsedInstruction.instruction());
				errs.addAll(parsedInstruction.errors());
			}
		}

		return results;

	}

	record InstructionAndErrors(Instruction instruction, List<ParseError> errors) {
	}

	private static InstructionAndErrors parseInstruction(JsonNode instNode, int index,
			Map<String, Integer> labeledBookmarks) {
		var errs = new ArrayList<ParseError>();
		var type = instNode.path("type").asText();
		final Instruction instruction;
		if (type.equals("exit")) {
			instruction = new Instruction.Exit();
		} else if (type.equals("copy")) {

			var argres = parseValue(instNode.path("arg"), List.of(index));
			errs.addAll(argres.errors());

			var resres = parseVarAccess((ObjectNode) instNode.path("res"), List.of(index));
			errs.addAll(resres.errors());

			instruction = new Instruction.Copy(argres.value, resres.value);
		} else if (type.equals("print")) {

			var argres = parseValue(instNode.path("arg"), List.of(index));
			errs.addAll(argres.errors());
			instruction = new Instruction.Print(argres.value);
		} else if (type.equals("label")) {

			var label = instNode.path("label");
			if (label.isTextual()) {
				labeledBookmarks.put(label.asText(), index);
				instruction = new Instruction.Label(label.asText());
			} else {
				errs.add(new ParseError(iep(index, "label"), "Label is not understood"));
				instruction = new Instruction.Exit();
			}

		} else if (type.equals("branch")) {

			var label = instNode.path("label");
			if (!label.isTextual()) {
				errs.add(new ParseError(iep(index, "label"), "Label is not understood"));

			}
			var arg = instNode.path("arg");
			var argPargeResult = parseValue(arg, List.of(index));

			errs.addAll(argPargeResult.errors());
			if (errs.size() > 0) {
				instruction = new Instruction.Exit();
			} else {
				instruction = new Instruction.Branch(argPargeResult.value(), label.asText());
			}

		} else if (type.equals("mkdisp")) {

			var res = instNode.path("res");
			var resResult = parseVarAccess((ObjectNode) res, List.of(index));

			errs.addAll(resResult.errors());
			if (errs.size() > 0) {
				instruction = new Instruction.Exit();
			} else {
				instruction = new Instruction.MakeDisplay(resResult.value());
			}

		} else if (type.equals("mkpromise")) {

			var res = instNode.path("res");
			var resResult = parseVarAccess((ObjectNode) res, List.of(index));

			errs.addAll(resResult.errors());
			if (errs.size() > 0) {
				instruction = new Instruction.Exit();
			} else {
				instruction = new Instruction.MakePromise(resResult.value());
			}

		}

		else if (type.equals("updisp")) {

			var inPromiseArg = instNode.path("inputPromiseArg");
			var inputPromiseResult = parseValue(inPromiseArg, List.of(index));

			var dispArg = instNode.path("dispArg");
			var dispArgResult = parseValue(dispArg, List.of(index));
			errs.addAll(dispArgResult.errors());

			var dispIdArg = instNode.path("dispIdArg");
			var dispIdArgResult = parseValue(dispIdArg, List.of(index));
			errs.addAll(dispIdArgResult.errors());

			if (errs.size() > 0) {
				instruction = new Instruction.Exit();
			} else {
				instruction = new Instruction.UpdateDisplay(inputPromiseResult.value(), dispArgResult.value(),
						dispIdArgResult.value());
			}

		} else if (type.equals("await")) {

			var argres = parseValue(instNode.path("arg"), List.of(index));
			errs.addAll(argres.errors());

			var resres = parseVarAccess((ObjectNode) instNode.path("res"), List.of(index));
			errs.addAll(resres.errors());

			instruction = new Instruction.AwaitPromise(argres.value, resres.value);
		}

		else if (type.equals("add")) {

			var argres1 = parseValue(instNode.path("arg1"), List.of(index));
			errs.addAll(argres1.errors());

			var argres2 = parseValue(instNode.path("arg2"), List.of(index));
			errs.addAll(argres2.errors());

			var resres = parseVarAccess((ObjectNode) instNode.path("res"), List.of(index));
			errs.addAll(resres.errors());

			instruction = new Instruction.Add(argres1.value, argres2.value, resres.value);
		}

		else if (type.equals("minus")) {

			var argres1 = parseValue(instNode.path("arg1"), List.of(index));
			errs.addAll(argres1.errors());

			var argres2 = parseValue(instNode.path("arg2"), List.of(index));
			errs.addAll(argres2.errors());

			var resres = parseVarAccess((ObjectNode) instNode.path("res"), List.of(index));
			errs.addAll(resres.errors());

			instruction = new Instruction.Minus(argres1.value, argres2.value, resres.value);
		}

		else if (type.equals("eq")) {

			var argres1 = parseValue(instNode.path("arg1"), List.of(index));
			errs.addAll(argres1.errors());

			var argres2 = parseValue(instNode.path("arg2"), List.of(index));
			errs.addAll(argres2.errors());

			var resres = parseVarAccess((ObjectNode) instNode.path("res"), List.of(index));
			errs.addAll(resres.errors());

			instruction = new Instruction.Eq(argres1.value, argres2.value, resres.value);
		}

		else if (type.equals("gt")) {

			var argres1 = parseValue(instNode.path("arg1"), List.of(index));
			errs.addAll(argres1.errors());

			var argres2 = parseValue(instNode.path("arg2"), List.of(index));
			errs.addAll(argres2.errors());

			var resres = parseVarAccess((ObjectNode) instNode.path("res"), List.of(index));
			errs.addAll(resres.errors());

			instruction = new Instruction.Gt(argres1.value, argres2.value, resres.value);
		}

		else if (type.equals("lt")) {

			var argres1 = parseValue(instNode.path("arg1"), List.of(index));
			errs.addAll(argres1.errors());

			var argres2 = parseValue(instNode.path("arg2"), List.of(index));
			errs.addAll(argres2.errors());

			var resres = parseVarAccess((ObjectNode) instNode.path("res"), List.of(index));
			errs.addAll(resres.errors());

			instruction = new Instruction.Lt(argres1.value, argres2.value, resres.value);
		} else if (type.equals("and")) {

			var argres1 = parseValue(instNode.path("arg1"), List.of(index));
			errs.addAll(argres1.errors());

			var argres2 = parseValue(instNode.path("arg2"), List.of(index));
			errs.addAll(argres2.errors());

			var resres = parseVarAccess((ObjectNode) instNode.path("res"), List.of(index));
			errs.addAll(resres.errors());

			instruction = new Instruction.And(argres1.value, argres2.value, resres.value);
		} else if (type.equals("or")) {

			var argres1 = parseValue(instNode.path("arg1"), List.of(index));
			errs.addAll(argres1.errors());

			var argres2 = parseValue(instNode.path("arg2"), List.of(index));
			errs.addAll(argres2.errors());

			var resres = parseVarAccess((ObjectNode) instNode.path("res"), List.of(index));
			errs.addAll(resres.errors());

			instruction = new Instruction.Or(argres1.value, argres2.value, resres.value);
		}

		else if (type.equals("deadend")) {
			instruction = new Instruction.DeadEnd();
		}

		else if (type.equals("parallel")) {

			var label = "";
			if (instNode.path("label").isTextual()) {
				label = instNode.path("label").asText();
			} else {
				errs.add(new ParseError(List.of(index), "label Missing"));
			}
			var copyList = new ArrayList<Instruction.Parallel.CopyItem>();
			if (instNode.path("copy").isArray()) {
				var copyNode = (ArrayNode) instNode.path("copy");

			
				for (var c = 0; c < copyNode.size(); c++) {
					var argValue = parseValue(copyNode.path(c).path("arg"), List.of(index, "copy", c));
					errs.addAll(argValue.errors());

					if (copyNode.path(c).path("res").isTextual()) {

						var resValue = copyNode.path(c).path("res").asText();


						copyList.add(new Instruction.Parallel.CopyItem(argValue.value, resValue));
					} else {
						errs.add(new ParseError(List.of(index, c), "Bad Res must be string"));
					}
				}
			}
			var refList = new ArrayList<Instruction.Parallel.RefItem>();
			if (instNode.path("ref").isArray()) {
				var refNode = (ArrayNode) instNode.path("ref");
				for(var r = 0; r< refNode.size(); r++){
					var targetName="";
					var refName="";
					if(refNode.path(r).path("targetName").isTextual()){
						targetName=refNode.path(r).path("targetName").asText();
					}
					if(refNode.path(r).path("refName").isTextual()){
						refName=refNode.path(r).path("refName").asText();
					}
					refList.add(new Instruction.Parallel.RefItem(targetName, refName));
				}
			}

			instruction = new Instruction.Parallel(label,copyList,refList);
		}
		else if (type.equals("respromise")) {

			var promiseIdArg = parseValue(instNode.path("promiseIdArg"), List.of(index));
			errs.addAll(promiseIdArg.errors());
			var valueArg = parseValue(instNode.path("valueArg"), List.of(index));
			errs.addAll(valueArg.errors());

			
			instruction = new Instruction.ResPromise(promiseIdArg.value, valueArg.value);
		}
		else {
			errs.add(new ParseError(iep(index, "type"), "notRecognized type " + type));
			instruction = new Instruction.Exit();
		}
		return new InstructionAndErrors(instruction, errs);
	}

	record ValueAndErrors(Data value, List<ParseError> errors) {
	}

	private static ValueAndErrors parseValue(JsonNode node, List<Object> path) {
		if (!node.isObject()) {
			return new ValueAndErrors(null,
					Arrays.asList(new ParseError(iep(path), "not an object")));
		}
		if (node.path("type").asText().equals("directVal")) {

			var res = parseDirectValue((ObjectNode) node, path);

			var z = res.value();
			return new ValueAndErrors(res.value, res.errors());
		} else if (node.path("type").asText().equals("varAccess")) {

			var res = parseVarAccess((ObjectNode) node, path);
			return new ValueAndErrors(res.value, res.errors());
		} else {

			var errpath = new ArrayList<Object>();
			errpath.addAll(path);
			errpath.add("type");
			return new ValueAndErrors(null,
					Arrays.asList(new ParseError(iep(errpath),
							"unexpected type" + node.path("type").asText())));

		}
	}

	record DirectValueAndErrors(Data.DirectValue value, List<ParseError> errors) {
	}

	private static DirectValueAndErrors parseDirectValue(ObjectNode node, List<Object> path) {
		if (node.path("value").isMissingNode()) {
			return new DirectValueAndErrors(null, Arrays.asList(new ParseError(iep(path)

					, "value missing")));
		}
		var dv = new Data.DirectValue(node.path("value"));
		List<ParseError> errs = List.of();

		return new DirectValueAndErrors(dv, errs);
	}

	record VarAccessAndErrors(Data.VariableAccess value, List<ParseError> errors) {
	}

	private static VarAccessAndErrors parseVarAccess(ObjectNode node, List<Object> path) {
		// TODO STUFF

		if (node.path("name").isMissingNode()) {
			return new VarAccessAndErrors(null, Arrays.asList(new ParseError(iep(path)

					, "name value missing")));
		}

		if (node.path("path").isMissingNode()) {
			return new VarAccessAndErrors(null, Arrays.asList(new ParseError(iep(path)

					, "path value missing")));
		}

		// TODO FIX PATH
		var pathparsed = parseValue(node.path("path"), path);

		if (pathparsed.errors().size() > 0) {
			return new VarAccessAndErrors(null, pathparsed.errors);
		}
		var dv = new Data.VariableAccess(
				node.path("name").asText(),
				pathparsed.value);
		List<ParseError> errs = List.of();

		return new VarAccessAndErrors(dv, errs);
	}

	public static List<Object> iep(List<Object> pathseg) {
		var ar = new ArrayList<Object>();
		ar.add("instructions");

		ar.addAll(Arrays.asList(pathseg));
		return ar;
	}

	public static List<Object> iep(Object... pathseg) {
		var ar = new ArrayList<Object>();
		ar.add("instructions");

		ar.addAll(Arrays.asList(pathseg));
		return ar;
	}

	public static JsonNode getValueAtPath(JsonNode source, JsonNode jsonpath) {

		var path = getListObjectJsonPathFromJsonNode(jsonpath);

		var curJ = source;
		for (var o : path) {

			curJ = switch (o) {
				case Integer i -> curJ.path(i);
				case String s -> curJ.path(s);

				default -> curJ;
			};

		}
		return curJ;
	}

	private static List<Object> getListObjectJsonPathFromJsonNode(JsonNode jsonpath) {
		var path = new ArrayList<Object>();

		if (jsonpath.isArray()) {
			var jar = (ArrayNode) jsonpath;
			for (var item : jar) {
				if (item.isTextual()) {
					path.add(item.asText());
				} else if (item.isInt()) {
					path.add(item.asInt());
				}

			}
		}
		return path;
	}

	public static JsonNode replaceValueAtPath(JsonNode source, JsonNode newValue, JsonNode jsonpath,
			ObjectMapper objectMapper) {

		var path = getListObjectJsonPathFromJsonNode(jsonpath);

		if (path.size() == 0) {
			return newValue;
		}

		var modified = source.deepCopy();
		var curJ = modified;

		for (var index = 0; index < (path.size() - 1); index++) {

			Object pathpart = path.get(index);
			var nextcurJ = switch (pathpart) {
				case Integer i -> curJ.path(i);
				case String s -> curJ.path(s);

				default -> curJ;
			};
			if (nextcurJ.isMissingNode()) {
				if (index == 0) {
					modified = switch (pathpart) {
						case Integer i -> {
							yield objectMapper.createArrayNode();
						}
						case String s -> {
							yield objectMapper.createObjectNode();
						}
						default -> {
							yield modified;
						}
					};
					curJ = modified;
				}
				nextcurJ = switch (path.get(index + 1)) {
					case Integer ni -> {
						yield switch (pathpart) {
							case Integer i -> {
								yield switch (curJ) {
									// previousnode
									case ArrayNode an -> {

										for (var arindex = 0; arindex < index; arindex++) {
											if (an.path(arindex).isMissingNode()) {
												an.addNull();
											}
										}
										an.addArray();
										yield an.path(i);

									}
									case ObjectNode on -> {
										throw new RuntimeException("notimplimentd");

									}

									default -> {
										throw new RuntimeException("notimplimentd");
									}
								};
							}
							case String s -> {
								yield switch (curJ) {
									// previousnode
									case ArrayNode an -> {
										throw new RuntimeException("notimplimentd");

									}
									case ObjectNode on -> {
										on.putArray(s);
										yield on.path(s);

									}

									default -> {
										throw new RuntimeException("notimplimentd");
									}
								};
							}
							default -> {
								yield curJ;
							}
						};
					}
					case String ns -> {
						yield switch (pathpart) {
							case Integer i -> {
								yield switch (curJ) {
									// previousnode
									case ArrayNode an -> {

										for (var arindex = 0; arindex < index; arindex++) {
											if (an.path(arindex).isMissingNode()) {
												an.addNull();
											}
										}
										an.addObject();
										yield an.path(i);

									}
									case ObjectNode on -> {
										throw new RuntimeException("notimplimentd");

									}

									default -> {
										throw new RuntimeException("notimplimentd");
									}
								};
							}
							case String s -> {
								yield switch (curJ) {
									// previousnode
									case ArrayNode an -> {
										throw new RuntimeException("notimplimentd");

									}
									case ObjectNode on -> {
										on.putObject(s);
										yield on.path(s);

									}

									default -> {
										throw new RuntimeException("notimplimentd");
									}
								};
							}
							default -> {
								yield curJ;
							}
						};

					}
					default -> {
						yield curJ;
					}

				};
			}

			curJ = nextcurJ;

		}

		switch (path.getLast()) {
			case Integer i -> {
				switch (curJ) {
					case ArrayNode an -> {
						for (var arindex = 0; arindex < i; arindex++) {
							if (an.path(arindex).isMissingNode()) {
								an.addNull();
							}
						}
						an.insert(i, newValue);

					}
					default -> {
					}

				}
			}
			case String s -> {
				switch (curJ) {
					case ObjectNode on -> {
						on.set(s, newValue);

					}
					default -> {
					}

				}
			}

			default -> {
			}
		}

		return modified;
	}

}
