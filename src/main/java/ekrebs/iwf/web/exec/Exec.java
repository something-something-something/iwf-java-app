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
	}

	interface InstructionVisitor<T> {

		T vist(Instruction.Copy inst);

		T vist(Instruction.Exit inst);

		T vist(Instruction.Print inst);

		T vist(Instruction.Label inst);

		T vist(Instruction.Branch inst);
	}

	public record ParseResult(List<Instruction> instructions, List<ParseError> errors,Map<String,Integer> labeledBookmarks) {
	}

	record ParseError(List<Object> path, String message) {
	}

	public static ParseResult parse(JsonNode jsonNode) {
		var labeledBookmarks=new HashMap<String,Integer>();
		var instructions = new ArrayList<Instruction>();
		var errs = new ArrayList<ParseError>();
		var results = new ParseResult(instructions, errs,labeledBookmarks);
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
				var parsedInstruction = parseInstruction(instNode, i,labeledBookmarks);

				instructions.add(parsedInstruction.instruction());
				errs.addAll(parsedInstruction.errors());
			}
		}

		return results;

	}

	record InstructionAndErrors(Instruction instruction, List<ParseError> errors) {
	}

	private static InstructionAndErrors parseInstruction(JsonNode instNode, int index, Map<String,Integer> labeledBookmarks) {
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
			var arg= instNode.path("arg");
			var argPargeResult =parseValue(arg, List.of(index));

			errs.addAll(argPargeResult.errors());
			if (errs.size() > 0) {
				instruction = new Instruction.Exit();
			} else {
				instruction = new Instruction.Branch(argPargeResult.value(),label.asText());
			}

		} else {
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

	record VarAccessAndErrors( Data.VariableAccess value, List<ParseError> errors) {
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
