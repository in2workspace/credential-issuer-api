# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v1.2.2](https://github.com/in2workspace/in2-issuer-api/releases/tag/v1.2.2)
### Added
- Add scheduled task to set EXPIRED status to credentials that have expired.

## [v1.2.1](https://github.com/in2workspace/in2-issuer-api/releases/tag/v1.2.1)
### Added
- Add support for requesting a fresh QR code if the previous one has expired or was an error during the proccess of

## [v1.2.0](https://github.com/in2workspace/in2-issuer-api/releases/tag/v1.2.0)
### Added
- Validation of authentication for issuance against the verifier.
- Verifiable Certifications issuance and sending to response_uri.
### Changed
- List credentials in order from newest to oldest.

## [v1.1.3](https://github.com/in2workspace/in2-issuer-api/releases/tag/v1.1.3)
### Changed
- Change the Credential Offer email template

## [v1.1.2](https://github.com/in2workspace/in2-issuer-api/releases/tag/v1.1.2)
### Changed
- Change the order of the received email from the pin during the issuance of a credential.

## [v1.1.1](https://github.com/in2workspace/in2-issuer-api/releases/tag/v1.1.1)
### Fixed
- Fixed LEARCredentialEmployee data model. Implement W3C DATA model v2.0 (validFrom, validUntil). 

## v1.1.0
### Added
- LEARCredentialEmployee issuance in a synchronous way.
- DOME Trust Framework integration to register issuers and participants.
### Changed
- Issuances API to support various issuance types.

## [Unreleased]: v0.7.0
- LEARCredential compliance.

## [Unreleased]: v0.6.0
- DOME profile compliance.

## [Unreleased]: v0.5.0
- Deferred credential emission.
- tx_code support for PIN.
- Persistence of emitted credentials in ddb.
- Retrieval and management of emitted credentials.

## [Unreleased]: v0.4.0
- Hexagonal pattern.
- Credential Offer endpoint requiere type of credential.
- DOME profile refactor and fixes.
- Batch credential support (extra)

## [Unreleased]: v0.3.0
- Support for credentials in JWT and CWT.
- Remove of external libraries for CV generation
- Native credential payload generation.
- Local emission.

## [Unreleased]: v0.2.0
- Adapter for Abstract Configuration loading.
- Support for Configurations from YAML file.
- Support for Configurations from Azure App Configuration.

## [Unreleased]: v0.1.0
- Successful build and tests.
- Compatibility with standard dependencies and plugins.
- Migration of files.

[release]:
