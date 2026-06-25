export const openApiConfig = {
  input: "../specs/001-digital-banking-auth/contracts/auth.openapi.yaml",
  output: "src/api/generated",
  client: "fetch",
  useOptions: true,
  useUnionTypes: true,
};
