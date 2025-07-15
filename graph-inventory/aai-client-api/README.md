# aai-client-api

This module is intended as a compatibility layer between the old and the new client.
The new client was not created to be fully interchangeable with the old client in the interface sense,
since that would force some of the design decisions upon the new client.

However the property loading uses the same interface via this module, to allow consumers of the two libraries
to reuse their implementations of `AAIProperties`.
