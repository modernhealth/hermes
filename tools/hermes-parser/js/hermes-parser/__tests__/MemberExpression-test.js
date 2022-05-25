/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @flow strict-local
 * @format
 */

'use strict';

import type {AlignmentCase} from '../__test_utils__/alignment-utils';

import {
  expectBabelAlignment,
  expectEspreeAlignment,
} from '../__test_utils__/alignment-utils';
import {parseForSnapshot} from '../__test_utils__/parse';

describe('MemberExpression', () => {
  describe('Non-computed', () => {
    const testCase: AlignmentCase = {
      code: `
        x.y;
      `,
      espree: {expectToFail: false},
      babel: {expectToFail: false},
    };

    test('ESTree', () => {
      expect(parseForSnapshot(testCase.code)).toMatchInlineSnapshot(`
        Object {
          "body": Array [
            Object {
              "directive": null,
              "expression": Object {
                "computed": false,
                "object": Object {
                  "name": "x",
                  "optional": false,
                  "type": "Identifier",
                  "typeAnnotation": null,
                },
                "optional": false,
                "property": Object {
                  "name": "y",
                  "optional": false,
                  "type": "Identifier",
                  "typeAnnotation": null,
                },
                "type": "MemberExpression",
              },
              "type": "ExpressionStatement",
            },
          ],
          "type": "Program",
        }
      `);
      expectEspreeAlignment(testCase);
    });

    test('Babel', () => {
      expectBabelAlignment(testCase);
    });
  });

  describe('Computed', () => {
    const testCase: AlignmentCase = {
      code: `
        x['y'];
      `,
      espree: {expectToFail: false},
      babel: {expectToFail: false},
    };

    test('ESTree', () => {
      expect(parseForSnapshot(testCase.code)).toMatchInlineSnapshot(`
        Object {
          "body": Array [
            Object {
              "directive": null,
              "expression": Object {
                "computed": true,
                "object": Object {
                  "name": "x",
                  "optional": false,
                  "type": "Identifier",
                  "typeAnnotation": null,
                },
                "optional": false,
                "property": Object {
                  "literalType": "string",
                  "raw": "'y'",
                  "type": "Literal",
                  "value": "y",
                },
                "type": "MemberExpression",
              },
              "type": "ExpressionStatement",
            },
          ],
          "type": "Program",
        }
      `);
      expectEspreeAlignment(testCase);
    });

    test('Babel', () => {
      expectBabelAlignment(testCase);
    });
  });
});

describe('OptionalMemberExpression', () => {
  describe('Non-computed', () => {
    const testCase: AlignmentCase = {
      code: `
        one?.two;
        one?.two.three;
        one.two?.three;
        one.two?.three.four;
        one.two?.three?.four;
      `,
      espree: {
        // TODO - ESTree spec is now ChainExpression + MemberExpression
        expectToFail: 'ast-diff',
      },
      babel: {expectToFail: false},
    };

    test('ESTree', () => {
      expect(parseForSnapshot(testCase.code)).toMatchInlineSnapshot(`
        Object {
          "body": Array [
            Object {
              "directive": null,
              "expression": Object {
                "computed": false,
                "object": Object {
                  "name": "one",
                  "optional": false,
                  "type": "Identifier",
                  "typeAnnotation": null,
                },
                "optional": true,
                "property": Object {
                  "name": "two",
                  "optional": false,
                  "type": "Identifier",
                  "typeAnnotation": null,
                },
                "type": "OptionalMemberExpression",
              },
              "type": "ExpressionStatement",
            },
            Object {
              "directive": null,
              "expression": Object {
                "computed": false,
                "object": Object {
                  "computed": false,
                  "object": Object {
                    "name": "one",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "optional": true,
                  "property": Object {
                    "name": "two",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "type": "OptionalMemberExpression",
                },
                "optional": false,
                "property": Object {
                  "name": "three",
                  "optional": false,
                  "type": "Identifier",
                  "typeAnnotation": null,
                },
                "type": "OptionalMemberExpression",
              },
              "type": "ExpressionStatement",
            },
            Object {
              "directive": null,
              "expression": Object {
                "computed": false,
                "object": Object {
                  "computed": false,
                  "object": Object {
                    "name": "one",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "optional": false,
                  "property": Object {
                    "name": "two",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "type": "MemberExpression",
                },
                "optional": true,
                "property": Object {
                  "name": "three",
                  "optional": false,
                  "type": "Identifier",
                  "typeAnnotation": null,
                },
                "type": "OptionalMemberExpression",
              },
              "type": "ExpressionStatement",
            },
            Object {
              "directive": null,
              "expression": Object {
                "computed": false,
                "object": Object {
                  "computed": false,
                  "object": Object {
                    "computed": false,
                    "object": Object {
                      "name": "one",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "optional": false,
                    "property": Object {
                      "name": "two",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "type": "MemberExpression",
                  },
                  "optional": true,
                  "property": Object {
                    "name": "three",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "type": "OptionalMemberExpression",
                },
                "optional": false,
                "property": Object {
                  "name": "four",
                  "optional": false,
                  "type": "Identifier",
                  "typeAnnotation": null,
                },
                "type": "OptionalMemberExpression",
              },
              "type": "ExpressionStatement",
            },
            Object {
              "directive": null,
              "expression": Object {
                "computed": false,
                "object": Object {
                  "computed": false,
                  "object": Object {
                    "computed": false,
                    "object": Object {
                      "name": "one",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "optional": false,
                    "property": Object {
                      "name": "two",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "type": "MemberExpression",
                  },
                  "optional": true,
                  "property": Object {
                    "name": "three",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "type": "OptionalMemberExpression",
                },
                "optional": true,
                "property": Object {
                  "name": "four",
                  "optional": false,
                  "type": "Identifier",
                  "typeAnnotation": null,
                },
                "type": "OptionalMemberExpression",
              },
              "type": "ExpressionStatement",
            },
          ],
          "type": "Program",
        }
      `);
      expectEspreeAlignment(testCase);
    });

    test('Babel', () => {
      expectBabelAlignment(testCase);
    });
  });

  describe('Computed', () => {
    const testCase: AlignmentCase = {
      code: `
        one?.[2];
        one?.[2][3];
        one[2]?.[3];
        one[2]?.[3];
        one[2]?.[3][4];
        one[2]?.[3]?.[4];
      `,
      espree: {
        // TODO - ESTree spec is now ChainExpression + MemberExpression
        expectToFail: 'ast-diff',
      },
      babel: {expectToFail: false},
    };

    test('ESTree', () => {
      expect(parseForSnapshot(testCase.code)).toMatchInlineSnapshot(`
        Object {
          "body": Array [
            Object {
              "directive": null,
              "expression": Object {
                "computed": true,
                "object": Object {
                  "name": "one",
                  "optional": false,
                  "type": "Identifier",
                  "typeAnnotation": null,
                },
                "optional": true,
                "property": Object {
                  "literalType": "numeric",
                  "raw": "2",
                  "type": "Literal",
                  "value": 2,
                },
                "type": "OptionalMemberExpression",
              },
              "type": "ExpressionStatement",
            },
            Object {
              "directive": null,
              "expression": Object {
                "computed": true,
                "object": Object {
                  "computed": true,
                  "object": Object {
                    "name": "one",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "optional": true,
                  "property": Object {
                    "literalType": "numeric",
                    "raw": "2",
                    "type": "Literal",
                    "value": 2,
                  },
                  "type": "OptionalMemberExpression",
                },
                "optional": false,
                "property": Object {
                  "literalType": "numeric",
                  "raw": "3",
                  "type": "Literal",
                  "value": 3,
                },
                "type": "OptionalMemberExpression",
              },
              "type": "ExpressionStatement",
            },
            Object {
              "directive": null,
              "expression": Object {
                "computed": true,
                "object": Object {
                  "computed": true,
                  "object": Object {
                    "name": "one",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "optional": false,
                  "property": Object {
                    "literalType": "numeric",
                    "raw": "2",
                    "type": "Literal",
                    "value": 2,
                  },
                  "type": "MemberExpression",
                },
                "optional": true,
                "property": Object {
                  "literalType": "numeric",
                  "raw": "3",
                  "type": "Literal",
                  "value": 3,
                },
                "type": "OptionalMemberExpression",
              },
              "type": "ExpressionStatement",
            },
            Object {
              "directive": null,
              "expression": Object {
                "computed": true,
                "object": Object {
                  "computed": true,
                  "object": Object {
                    "name": "one",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "optional": false,
                  "property": Object {
                    "literalType": "numeric",
                    "raw": "2",
                    "type": "Literal",
                    "value": 2,
                  },
                  "type": "MemberExpression",
                },
                "optional": true,
                "property": Object {
                  "literalType": "numeric",
                  "raw": "3",
                  "type": "Literal",
                  "value": 3,
                },
                "type": "OptionalMemberExpression",
              },
              "type": "ExpressionStatement",
            },
            Object {
              "directive": null,
              "expression": Object {
                "computed": true,
                "object": Object {
                  "computed": true,
                  "object": Object {
                    "computed": true,
                    "object": Object {
                      "name": "one",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "optional": false,
                    "property": Object {
                      "literalType": "numeric",
                      "raw": "2",
                      "type": "Literal",
                      "value": 2,
                    },
                    "type": "MemberExpression",
                  },
                  "optional": true,
                  "property": Object {
                    "literalType": "numeric",
                    "raw": "3",
                    "type": "Literal",
                    "value": 3,
                  },
                  "type": "OptionalMemberExpression",
                },
                "optional": false,
                "property": Object {
                  "literalType": "numeric",
                  "raw": "4",
                  "type": "Literal",
                  "value": 4,
                },
                "type": "OptionalMemberExpression",
              },
              "type": "ExpressionStatement",
            },
            Object {
              "directive": null,
              "expression": Object {
                "computed": true,
                "object": Object {
                  "computed": true,
                  "object": Object {
                    "computed": true,
                    "object": Object {
                      "name": "one",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "optional": false,
                    "property": Object {
                      "literalType": "numeric",
                      "raw": "2",
                      "type": "Literal",
                      "value": 2,
                    },
                    "type": "MemberExpression",
                  },
                  "optional": true,
                  "property": Object {
                    "literalType": "numeric",
                    "raw": "3",
                    "type": "Literal",
                    "value": 3,
                  },
                  "type": "OptionalMemberExpression",
                },
                "optional": true,
                "property": Object {
                  "literalType": "numeric",
                  "raw": "4",
                  "type": "Literal",
                  "value": 4,
                },
                "type": "OptionalMemberExpression",
              },
              "type": "ExpressionStatement",
            },
          ],
          "type": "Program",
        }
      `);
      expectEspreeAlignment(testCase);
    });

    test('Babel', () => {
      expectBabelAlignment(testCase);
    });
  });

  // This is a special case to test because of the way parentheses
  // short-circuit the optional chain
  describe('With parentheses', () => {
    describe('Non-computed', () => {
      const testCase: AlignmentCase = {
        code: `
          (one?.two);
          (one?.two).three;
          (one.two?.three);
          (one.two?.three).four;
          (one.two?.three?.four);
          (one.two?.three?.four).five;
        `,
        espree: {
          // TODO - ESTree spec is now ChainExpression + MemberExpression
          expectToFail: 'ast-diff',
        },
        babel: {expectToFail: false},
      };

      test('ESTree', () => {
        expect(parseForSnapshot(testCase.code)).toMatchInlineSnapshot(`
          Object {
            "body": Array [
              Object {
                "directive": null,
                "expression": Object {
                  "computed": false,
                  "object": Object {
                    "name": "one",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "optional": true,
                  "property": Object {
                    "name": "two",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "type": "OptionalMemberExpression",
                },
                "type": "ExpressionStatement",
              },
              Object {
                "directive": null,
                "expression": Object {
                  "computed": false,
                  "object": Object {
                    "computed": false,
                    "object": Object {
                      "name": "one",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "optional": true,
                    "property": Object {
                      "name": "two",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "type": "OptionalMemberExpression",
                  },
                  "optional": false,
                  "property": Object {
                    "name": "three",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "type": "MemberExpression",
                },
                "type": "ExpressionStatement",
              },
              Object {
                "directive": null,
                "expression": Object {
                  "computed": false,
                  "object": Object {
                    "computed": false,
                    "object": Object {
                      "name": "one",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "optional": false,
                    "property": Object {
                      "name": "two",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "type": "MemberExpression",
                  },
                  "optional": true,
                  "property": Object {
                    "name": "three",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "type": "OptionalMemberExpression",
                },
                "type": "ExpressionStatement",
              },
              Object {
                "directive": null,
                "expression": Object {
                  "computed": false,
                  "object": Object {
                    "computed": false,
                    "object": Object {
                      "computed": false,
                      "object": Object {
                        "name": "one",
                        "optional": false,
                        "type": "Identifier",
                        "typeAnnotation": null,
                      },
                      "optional": false,
                      "property": Object {
                        "name": "two",
                        "optional": false,
                        "type": "Identifier",
                        "typeAnnotation": null,
                      },
                      "type": "MemberExpression",
                    },
                    "optional": true,
                    "property": Object {
                      "name": "three",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "type": "OptionalMemberExpression",
                  },
                  "optional": false,
                  "property": Object {
                    "name": "four",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "type": "MemberExpression",
                },
                "type": "ExpressionStatement",
              },
              Object {
                "directive": null,
                "expression": Object {
                  "computed": false,
                  "object": Object {
                    "computed": false,
                    "object": Object {
                      "computed": false,
                      "object": Object {
                        "name": "one",
                        "optional": false,
                        "type": "Identifier",
                        "typeAnnotation": null,
                      },
                      "optional": false,
                      "property": Object {
                        "name": "two",
                        "optional": false,
                        "type": "Identifier",
                        "typeAnnotation": null,
                      },
                      "type": "MemberExpression",
                    },
                    "optional": true,
                    "property": Object {
                      "name": "three",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "type": "OptionalMemberExpression",
                  },
                  "optional": true,
                  "property": Object {
                    "name": "four",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "type": "OptionalMemberExpression",
                },
                "type": "ExpressionStatement",
              },
              Object {
                "directive": null,
                "expression": Object {
                  "computed": false,
                  "object": Object {
                    "computed": false,
                    "object": Object {
                      "computed": false,
                      "object": Object {
                        "computed": false,
                        "object": Object {
                          "name": "one",
                          "optional": false,
                          "type": "Identifier",
                          "typeAnnotation": null,
                        },
                        "optional": false,
                        "property": Object {
                          "name": "two",
                          "optional": false,
                          "type": "Identifier",
                          "typeAnnotation": null,
                        },
                        "type": "MemberExpression",
                      },
                      "optional": true,
                      "property": Object {
                        "name": "three",
                        "optional": false,
                        "type": "Identifier",
                        "typeAnnotation": null,
                      },
                      "type": "OptionalMemberExpression",
                    },
                    "optional": true,
                    "property": Object {
                      "name": "four",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "type": "OptionalMemberExpression",
                  },
                  "optional": false,
                  "property": Object {
                    "name": "five",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "type": "MemberExpression",
                },
                "type": "ExpressionStatement",
              },
            ],
            "type": "Program",
          }
        `);
        expectEspreeAlignment(testCase);
      });

      test('Babel', () => {
        expectBabelAlignment(testCase);
      });
    });

    describe('Computed', () => {
      const testCase: AlignmentCase = {
        code: `
          (one?.[2]);
          (one?.[2])[3];
          (one[2]?.[3]);
          (one[2]?.[3])[4];
          (one[2]?.[3]?.[4]);
          (one[2]?.[3]?.[4])[5];
        `,
        espree: {
          // TODO - ESTree spec is now ChainExpression + MemberExpression
          expectToFail: 'ast-diff',
        },
        babel: {expectToFail: false},
      };

      test('ESTree', () => {
        expect(parseForSnapshot(testCase.code)).toMatchInlineSnapshot(`
          Object {
            "body": Array [
              Object {
                "directive": null,
                "expression": Object {
                  "computed": true,
                  "object": Object {
                    "name": "one",
                    "optional": false,
                    "type": "Identifier",
                    "typeAnnotation": null,
                  },
                  "optional": true,
                  "property": Object {
                    "literalType": "numeric",
                    "raw": "2",
                    "type": "Literal",
                    "value": 2,
                  },
                  "type": "OptionalMemberExpression",
                },
                "type": "ExpressionStatement",
              },
              Object {
                "directive": null,
                "expression": Object {
                  "computed": true,
                  "object": Object {
                    "computed": true,
                    "object": Object {
                      "name": "one",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "optional": true,
                    "property": Object {
                      "literalType": "numeric",
                      "raw": "2",
                      "type": "Literal",
                      "value": 2,
                    },
                    "type": "OptionalMemberExpression",
                  },
                  "optional": false,
                  "property": Object {
                    "literalType": "numeric",
                    "raw": "3",
                    "type": "Literal",
                    "value": 3,
                  },
                  "type": "MemberExpression",
                },
                "type": "ExpressionStatement",
              },
              Object {
                "directive": null,
                "expression": Object {
                  "computed": true,
                  "object": Object {
                    "computed": true,
                    "object": Object {
                      "name": "one",
                      "optional": false,
                      "type": "Identifier",
                      "typeAnnotation": null,
                    },
                    "optional": false,
                    "property": Object {
                      "literalType": "numeric",
                      "raw": "2",
                      "type": "Literal",
                      "value": 2,
                    },
                    "type": "MemberExpression",
                  },
                  "optional": true,
                  "property": Object {
                    "literalType": "numeric",
                    "raw": "3",
                    "type": "Literal",
                    "value": 3,
                  },
                  "type": "OptionalMemberExpression",
                },
                "type": "ExpressionStatement",
              },
              Object {
                "directive": null,
                "expression": Object {
                  "computed": true,
                  "object": Object {
                    "computed": true,
                    "object": Object {
                      "computed": true,
                      "object": Object {
                        "name": "one",
                        "optional": false,
                        "type": "Identifier",
                        "typeAnnotation": null,
                      },
                      "optional": false,
                      "property": Object {
                        "literalType": "numeric",
                        "raw": "2",
                        "type": "Literal",
                        "value": 2,
                      },
                      "type": "MemberExpression",
                    },
                    "optional": true,
                    "property": Object {
                      "literalType": "numeric",
                      "raw": "3",
                      "type": "Literal",
                      "value": 3,
                    },
                    "type": "OptionalMemberExpression",
                  },
                  "optional": false,
                  "property": Object {
                    "literalType": "numeric",
                    "raw": "4",
                    "type": "Literal",
                    "value": 4,
                  },
                  "type": "MemberExpression",
                },
                "type": "ExpressionStatement",
              },
              Object {
                "directive": null,
                "expression": Object {
                  "computed": true,
                  "object": Object {
                    "computed": true,
                    "object": Object {
                      "computed": true,
                      "object": Object {
                        "name": "one",
                        "optional": false,
                        "type": "Identifier",
                        "typeAnnotation": null,
                      },
                      "optional": false,
                      "property": Object {
                        "literalType": "numeric",
                        "raw": "2",
                        "type": "Literal",
                        "value": 2,
                      },
                      "type": "MemberExpression",
                    },
                    "optional": true,
                    "property": Object {
                      "literalType": "numeric",
                      "raw": "3",
                      "type": "Literal",
                      "value": 3,
                    },
                    "type": "OptionalMemberExpression",
                  },
                  "optional": true,
                  "property": Object {
                    "literalType": "numeric",
                    "raw": "4",
                    "type": "Literal",
                    "value": 4,
                  },
                  "type": "OptionalMemberExpression",
                },
                "type": "ExpressionStatement",
              },
              Object {
                "directive": null,
                "expression": Object {
                  "computed": true,
                  "object": Object {
                    "computed": true,
                    "object": Object {
                      "computed": true,
                      "object": Object {
                        "computed": true,
                        "object": Object {
                          "name": "one",
                          "optional": false,
                          "type": "Identifier",
                          "typeAnnotation": null,
                        },
                        "optional": false,
                        "property": Object {
                          "literalType": "numeric",
                          "raw": "2",
                          "type": "Literal",
                          "value": 2,
                        },
                        "type": "MemberExpression",
                      },
                      "optional": true,
                      "property": Object {
                        "literalType": "numeric",
                        "raw": "3",
                        "type": "Literal",
                        "value": 3,
                      },
                      "type": "OptionalMemberExpression",
                    },
                    "optional": true,
                    "property": Object {
                      "literalType": "numeric",
                      "raw": "4",
                      "type": "Literal",
                      "value": 4,
                    },
                    "type": "OptionalMemberExpression",
                  },
                  "optional": false,
                  "property": Object {
                    "literalType": "numeric",
                    "raw": "5",
                    "type": "Literal",
                    "value": 5,
                  },
                  "type": "MemberExpression",
                },
                "type": "ExpressionStatement",
              },
            ],
            "type": "Program",
          }
        `);
        expectEspreeAlignment(testCase);
      });

      test('Babel', () => {
        expectBabelAlignment(testCase);
      });
    });
  });
});
