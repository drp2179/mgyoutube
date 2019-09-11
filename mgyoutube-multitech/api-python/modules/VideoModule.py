from abc import ABC, abstractmethod


class VideoModule(ABC):
    @abstractmethod
    def search(self, searchTerms: str) -> list:
        pass
